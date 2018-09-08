package eu.quelltext.mundraub.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.initialization.Permissions;
import eu.quelltext.mundraub.map.PlantsCache;

public class SettingsActivity extends MundraubBaseActivity {

    private ProgressBar updateProgress;
    final Handler handler = new Handler();
    ProgressUpdate progressAutoUpdate;

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
    }

    private void updateOrHideUpdateProgress() {
        Progress progress = PlantsCache.getUpdateProgressOrNull();
        if (progress == null) {
            updateProgress.setVisibility(View.GONE);
        } else {
            updateProgress.setVisibility(View.VISIBLE);
            int max = 100;
            updateProgress.setProgress((int)Math.round(max * progress.getProgress()));
            // update color from https://stackoverflow.com/a/15809803
            Drawable progressDrawable = updateProgress.getProgressDrawable().mutate();
            int color = progress.isDoneAndError() ? Color.RED : Color.GREEN;
            progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
            updateProgress.setProgressDrawable(progressDrawable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        progressAutoUpdate = new ProgressUpdate();
        progressAutoUpdate.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressAutoUpdate.stop();
    }

    private void update() {
        synchronizeBooleanSetting(R.id.toggle_API,  new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                if (checked) {
                    getPermissions().INTERNET.askIfNotGranted();
                }
                return Settings.useMundraubAPI(checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.useMundraubAPI();
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
                                        Settings.useOfflineMapAPI(true);
                                        update();
                                    }
                                });
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
                        Settings.useOfflineMapAPI(true);
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
        final SettingsActivity me = this;
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int result = onToggle.onToggle(isChecked);
                if (result != Settings.COMMIT_SUCCESSFUL) {
                    new Dialog(me).alertError(result);
                }
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
