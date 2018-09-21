package eu.quelltext.mundraub.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.initialization.FruitRadarNotification;
import eu.quelltext.mundraub.initialization.Permissions;
import eu.quelltext.mundraub.map.PlantsCache;

public class SettingsActivity extends MundraubBaseActivity {

    private ProgressBar updateProgress;
    final Handler handler = new Handler();
    ProgressUpdate progressAutoUpdate;
    private RadioGroup apiRadioGroup;
    private TextView textFruitRadarDistanceExplanation;
    private EditText textFruitRadarDistance;
    private Button buttonTestNotification;

    class ProgressUpdate implements Runnable {
        boolean stopped = false;
        @Override
        public void run() {
            try {
                updateOrHideUpdateProgress();
            } finally {
                if (!stopped) {
                    handler.postDelayed(this, 500);
                }
            }
        }

        public void stop() {
            stopped = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        updateProgress = (ProgressBar) findViewById(R.id.update_progress);
        apiRadioGroup = (RadioGroup) findViewById(R.id.api_choice);
        textFruitRadarDistanceExplanation = (TextView) findViewById(R.id.text_distance_in_meters_explanation);
        textFruitRadarDistance = (EditText) findViewById(R.id.number_meters_to_plant);
        textFruitRadarDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = textFruitRadarDistance.getText().toString();
                if (text.isEmpty()) {
                    return;
                }
                int meters = Integer.parseInt(text);
                if (meters != Settings.getRadarPlantRangeMeters()) {
                    feedbackAboutSettingsChange(Settings.setRadarPlantRangeMeters(meters));
                    setFruitradarDistanceText();
                }
            }
        });
        buttonTestNotification = (Button) findViewById(R.id.button_show_example_notification);
        buttonTestNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FruitRadarNotification.showExample();
            }
        });

    }

    @SuppressLint("StringFormatInvalid")
    private void setFruitradarDistanceText() {
        textFruitRadarDistanceExplanation.setText(
                String.format(
                        getString(R.string.settings_distance_in_meters),
                        Settings.getRadarPlantRangeMeters()));
    }

    private void updateOrHideUpdateProgress() {
        Progress progress = PlantsCache.getUpdateProgressOrNull();
        if (progress == null) {
            updateProgress.setVisibility(View.GONE);
        } else {
            int max = 100;
            int newProgress = (int) Math.round(max * progress.getProgress());
            if (newProgress != updateProgress.getProgress()) {
                updateProgress.setVisibility(View.VISIBLE);
                updateProgress.setProgress(newProgress);
                // update color from https://stackoverflow.com/a/15809803
                Drawable progressDrawable = updateProgress.getProgressDrawable().mutate();
                int color = progress.isDoneAndError() ? Color.RED : Color.GREEN;
                progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
                updateProgress.setProgressDrawable(progressDrawable);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        progressAutoUpdate = new ProgressUpdate();
        progressAutoUpdate.run();
        setFruitradarDistanceText();
        textFruitRadarDistance.setText(Integer.toString(Settings.getRadarPlantRangeMeters()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressAutoUpdate.stop();
    }

    private void update() {
        apiRadioGroup.check(API.instance().radioButtonId());
        apiRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (API api: API.all()) {
                    if (checkedId == api.radioButtonId()) {
                        feedbackAboutSettingsChange(Settings.useAPI(api));
                    }
                }
                update();
            }
        });
        synchronizeBooleanSetting(R.id.toggle_secure_connection, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.useInsecureConnections(!checked);
            }

            @Override
            public boolean isChecked() {
                return !Settings.useInsecureConnections();
            }
        });
        synchronizeBooleanSetting(R.id.toggle_cache, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (!checked) {
                    getPermissions().WRITE_EXTERNAL_STORAGE.askIfNotGranted();
                }
                return Settings.useCacheForPlants(checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.useCacheForPlants();
            }
        });
        synchronizeBooleanSetting(R.id.toggle_error_report, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    getPermissions().WRITE_EXTERNAL_STORAGE.askIfNotGranted();
                }
                return Settings.useErrorReport(checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.useErrorReport();
            }
        });
        synchronizeBooleanSetting(R.id.toggle_public_api, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    getPermissions().INTERNET.askIfNotGranted();
                }
                return Settings.debugMundraubMapAPI(checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.debugMundraubMapAPI();
            }
        });
        synchronizeBooleanSetting(R.id.toggle_offline_mode, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    if (!Settings.useOfflineMapAPI()) {
                        askDownloadOfflineMapData();
                    }
                } else {
                    getPermissions().INTERNET.askIfNotGranted();
                    return Settings.useOfflineMapAPI(false);
                }
                return Settings.COMMIT_SUCCESSFUL;
            }

            @Override
            public boolean isChecked() {
                return Settings.useOfflineMapAPI();
            }
        });
        synchronizePermissionSetting(R.id.toggle_camera, R.id.toggle_camera_ask, getPermissions().CAMERA);
        synchronizePermissionSetting(R.id.toggle_location, R.id.toggle_location_ask, getPermissions().ACCESS_FINE_LOCATION);
        synchronizePermissionSetting(R.id.toggle_internet, R.id.toggle_internet_ask, getPermissions().INTERNET);
        synchronizePermissionSetting(R.id.toggle_storage, R.id.toggle_storage_ask, getPermissions().WRITE_EXTERNAL_STORAGE);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_mundraub, Settings.API_ID_MUNDRAUB);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_na_ovoce, Settings.API_ID_NA_OVOCE);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_fruitmap, Settings.API_ID_FRUITMAP);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_community, Settings.API_ID_COMMUNITY);

        synchronizeCheckbutton(R.id.checkBox_fruit_radar, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    if (!Settings.useOfflineMapAPI()) {
                        new Dialog(SettingsActivity.this).askYesNo(
                                R.string.reason_radar_only_works_in_offline_mode,
                                R.string.ask_switch_on_offline_mode,
                                new Dialog.YesNoCallback() {
                                    @Override
                                    public void yes() {
                                        checkPermissions(true);
                                    }

                                    @Override
                                    public void no() {

                                    }
                                });
                    } else {
                        checkPermissions(false);
                    }
                } else {
                    return Settings.useFruitRadarNotifications(false);
                }
                return Settings.COMMIT_SUCCESSFUL;
            }

            private void checkPermissions(final boolean goOffline) {
                getPermissions().ACCESS_FINE_LOCATION.askIfNotGranted(new Permissions.PermissionChange() {
                    @Override
                    public void onGranted(Permissions.Permission permission) {
                        if (goOffline) {
                            askDownloadOfflineMapData();
                        }
                        feedbackAboutSettingsChange(Settings.useFruitRadarNotifications(true));
                        update();
                    }

                    @Override
                    public void onDenied(Permissions.Permission permission) {
                    }
                });
            }

            @Override
            public boolean isChecked() {
                return Settings.useFruitRadarNotifications();
            }
        });
        synchronizeCheckbutton(R.id.checkBox_fruit_radar_vibrate, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    getPermissions().VIBRATE.askIfNotGranted(new Permissions.PermissionChange() {
                        @Override
                        public void onGranted(Permissions.Permission permission) {
                            feedbackAboutSettingsChange(Settings.vibrateWhenPlantIsInRange(true));
                        }

                        @Override
                        public void onDenied(Permissions.Permission permission) {
                        }
                    });
                } else {
                    return Settings.vibrateWhenPlantIsInRange(false);
                }
                return Settings.COMMIT_SUCCESSFUL;
            }

            @Override
            public boolean isChecked() {
                return Settings.vibrateWhenPlantIsInRange();
            }
        });
    }

    private void askDownloadOfflineMapData() {
        new Dialog(SettingsActivity.this).askYesNo(
                R.string.reason_offline_data_needs_to_be_downloaded,
                R.string.ask_download_offline_data,
                new Dialog.YesNoCallback() {
                    @Override
                    public void yes() {
                        goOffline();
                    }

                    @Override
                    public void no() {
                        feedbackAboutSettingsChange(Settings.useOfflineMapAPI(true));
                        update();
                    }
                });
    }

    private void synchronizeCategoryCheckbutton(int checkBox_markers, final String apiId) {
        synchronizeCheckbutton(checkBox_markers, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.showCategory(apiId, checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.showCategory(apiId);
            }
        });
    }

    private void goOffline() {
        getPermissions().INTERNET.askIfNotGranted(new Permissions.PermissionChange() {
            @Override
            public void onDenied(Permissions.Permission permission) {}

            @Override
            public void onGranted(Permissions.Permission permission) {
                PlantsCache.update(new API.Callback() {
                    @Override
                    public void onSuccess() {
                        feedbackAboutSettingsChange(Settings.useOfflineMapAPI(true));
                        update();
                        new Dialog(SettingsActivity.this).alertSuccess(R.string.success_offline_data_was_downloaded);
                    }

                    @Override
                    public void onFailure(int errorResourceString) {
                        new Dialog(SettingsActivity.this).alertError(errorResourceString);
                    }
                });
            }
        });
    }

    interface Toggled {
        int onToggle(boolean checked);
        boolean isChecked();
    }

    private void synchronizeBooleanSetting(final int resourceId, final Toggled onToggle) {
        final ToggleButton toggle = (ToggleButton) findViewById(resourceId);
        toggle.setChecked(onToggle.isChecked());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                feedbackAboutSettingsChange(onToggle.onToggle(isChecked));
                update();
            }
        });
    }

    private void feedbackAboutSettingsChange(int resourceId) {
        if (resourceId != Settings.COMMIT_SUCCESSFUL) {
            new Dialog(SettingsActivity.this).alertError(resourceId);
        }
    }

    private void synchronizeCheckbutton(final int resourceId, final Toggled onToggle) {
        final CheckBox toggle = (CheckBox) findViewById(resourceId);
        toggle.setChecked(onToggle.isChecked());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                feedbackAboutSettingsChange(onToggle.onToggle(isChecked));
                update();
            }
        });
    }

    void synchronizePermissionSetting(int onOffId, int askId, final Permissions.Permission permission) {
        final ToggleButton onOff = (ToggleButton) findViewById(onOffId);
        final ToggleButton ask = (ToggleButton) findViewById(askId);
        onOff.setChecked(permission.isGranted());
        final SettingsActivity me = this;
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    permission.check();
                } else if (permission.isGranted()) {
                    // todo open settings to change app permissions
                    if (Permissions.CAN_ASK_FOR_PERMISSIONS) {
                        new Dialog(me).alertInfo(R.string.error_can_edit_permissions_only_externally);
                    } else {
                        new Dialog(me).alertInfo(R.string.error_can_not_edit_permissions_api_too_old);
                    }
                }
                update();
            }
        });
        ask.setChecked(permission.canAsk());
        ask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int result = permission.canAsk(isChecked);;
                if (result != Settings.COMMIT_SUCCESSFUL) {
                    new Dialog(me).alertError(result);
                }
                update();
            }
        });
    }

    @Override
    protected void openSettings() {
        // do nothing
    }
}
