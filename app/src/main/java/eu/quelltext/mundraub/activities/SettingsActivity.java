package eu.quelltext.mundraub.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.map.SelectOfflineMapPartsActivity;
import eu.quelltext.mundraub.activities.map.TestFruitRadarActivity;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.api.AsyncNetworkInteraction;
import eu.quelltext.mundraub.api.BackgroundDownloadTask;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.initialization.Permissions;
import eu.quelltext.mundraub.map.PlantsCache;
import eu.quelltext.mundraub.map.TilesCache;
import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.BoundingBoxCollection;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class SettingsActivity extends MundraubBaseActivity {

    private static BackgroundDownloadTask mapDownload = null;

    private ProgressBar updateProgress;
    final Handler handler = new Handler();
    ProgressUpdate plantProgressAutoUpdate;
    private RadioGroup apiRadioGroup;
    private TextView textFruitRadarDistanceExplanation;
    private EditText textFruitRadarDistance;
    private Button buttonTestNotification;
    private Button buttonOpenOfflineAreaChoice;
    private Button buttonRemoveAreas;
    private TextView offlineStatisticsText;
    private Button buttonDownloadMap;
    private ProgressBar updateProgressMap;
    private ProgressUpdate mapProgressAutoUpdate;

    abstract class ProgressUpdate implements Runnable {

        private final ProgressBar progressBar;

        private ProgressUpdate(ProgressBar bar) {
            this.progressBar = bar;
        }

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

        private void updateOrHideUpdateProgress() {
            Progress progress = getProgressOrNull();
            if (progress == null) {
                progressBar.setVisibility(View.GONE);
            } else {
                int max = 100;
                int newProgress = (int) Math.round(max * progress.getProgress());
                if (newProgress != progressBar.getProgress()) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                    // update color from https://stackoverflow.com/a/15809803
                    Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
                    int color = progress.isDoneAndError() ? Color.RED : Color.GREEN;
                    progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.MULTIPLY);
                    progressBar.setProgressDrawable(progressDrawable);
                }
            }
        }

        protected abstract Progress getProgressOrNull();
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
                Intent intent = new Intent(SettingsActivity.this, TestFruitRadarActivity.class);
                startActivity(intent);
            }
        });

        buttonOpenOfflineAreaChoice = (Button) findViewById(R.id.button_mark_offline_areas);
        buttonOpenOfflineAreaChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, SelectOfflineMapPartsActivity.class);
                startActivity(intent);
            }
        });

        buttonRemoveAreas = (Button) findViewById(R.id.button_remove_areas);
        buttonRemoveAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.setOfflineAreaBoundingBoxes(BoundingBoxCollection.empty());
                setOfflineMapStatisticsText();
                new Dialog(SettingsActivity.this).askYesNo(
                        R.string.settings_reason_delete_offline_tiles,
                        R.string.settings_ask_delete_downloaded_tiles,
                        new Dialog.YesNoCallback() {
                            @Override
                            public void yes() {
                                Helper.deleteDir(Settings.mapTilesCacheDirectory(SettingsActivity.this));
                                setOfflineMapStatisticsText();
                            }

                            @Override
                            public void no() {
                            }
                        });
            }
        });
        offlineStatisticsText = (TextView) findViewById(R.id.text_offline_map_statistics);

        buttonDownloadMap = (Button) findViewById(R.id.button_start_map_download);
        buttonDownloadMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloadingMap()) {
                    downloadMap();
                }
            }
        });
        updateProgressMap = (ProgressBar) findViewById(R.id.update_progress_map);
        updateMapOfflineButtons();
    }

    private void downloadMap() {
        mapDownload = new BackgroundDownloadTask();
        for (final TilesCache cache : Settings.getDownloadMaps()) {
            for (int zoom : Settings.getDownloadZoomLevels()) {
                for (BoundingBox bbox : Settings.getOfflineAreaBoundingBoxes().asSet()) {
                    for (final TilesCache.Tile tile : cache.getTilesIn(bbox, zoom)) {
                        if (tile.isCached()) {
                            continue;
                        }
                        mapDownload.collectDownloadsFrom(new BackgroundDownloadTask.DownloadProvider() {
                            @Override
                            public Set<String> getDownloadUrls() {
                                return new HashSet<>(Arrays.asList(tile.url()));
                            }

                            @Override
                            public void handleContent(ResponseBody body, Progressable fraction) throws IOException {
                                tile.file().getParentFile().mkdirs();
                                BufferedSink sink = Okio.buffer(Okio.sink(tile.file()));
                                sink.writeAll(body.source());
                                sink.close();
                                fraction.setProgress(1);
                            }

                            @Override
                            public double getDownloadFraction() {
                                return 0.8;
                            }
                        });
                    }
                }
            }
        }
        mapDownload.downloadInBackground(AsyncNetworkInteraction.Callback.NULL);
        updateMapOfflineButtons();
    }

    private void updateMapOfflineButtons() {
        buttonDownloadMap.setEnabled(!isDownloadingMap());
        buttonRemoveAreas.setEnabled(!isDownloadingMap());
        if (isDownloadingMap()) {
            mapDownload.getProgress().addCallback(new AsyncNetworkInteraction.Callback() {

                private void onDone() {
                    buttonDownloadMap.setEnabled(true);
                    buttonRemoveAreas.setEnabled(true);
                    setOfflineMapStatisticsText();
                }
                @Override
                public void onSuccess() {
                    onDone();
                }
                @Override
                public void onFailure(int errorResourceString) {
                    onDone();
                }
            });
        }
    }

    private boolean isDownloadingMap() {
        return mapDownload != null && !mapDownload.getProgress().isDone();
    }

    @SuppressLint("StringFormatInvalid")
    private void setFruitradarDistanceText() {
        textFruitRadarDistanceExplanation.setText(
                String.format(
                        getString(R.string.settings_distance_in_meters),
                        Settings.getRadarPlantRangeMeters()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        resumeProgressBars();
        setFruitradarDistanceText();
        textFruitRadarDistance.setText(Integer.toString(Settings.getRadarPlantRangeMeters()));
        setOfflineMapStatisticsText();
    }

    private void resumeProgressBars() {
        plantProgressAutoUpdate = new ProgressUpdate(updateProgress) {
            @Override
            protected Progress getProgressOrNull() {
                return PlantsCache.getUpdateProgressOrNull();
            }
        };
        plantProgressAutoUpdate.run();
        mapProgressAutoUpdate = new ProgressUpdate(updateProgressMap) {
            @Override
            protected Progress getProgressOrNull() {
                if (mapDownload == null) {
                    return null;
                }
                return mapDownload.getProgress();
            }
        };
        mapProgressAutoUpdate.run();
    }

    @SuppressLint("StringFormatInvalid")
    private void setOfflineMapStatisticsText() {
        String template = getString(R.string.settings_offline_map_statistics);
        BoundingBoxCollection bboxes = Settings.getOfflineAreaBoundingBoxes();
        String estimatedBytes = bboxes.byteCountToHumanReadableString(bboxes.estimateTileBytesIn(Settings.getDownloadMaps(), Settings.getDownloadZoomLevels()));
        String usedBytes = bboxes.byteCountToHumanReadableString(Helper.folderSize(Settings.mapTilesCacheDirectory(this)));
        String text = String.format(template, bboxes.size(), estimatedBytes, usedBytes);
        offlineStatisticsText.setText(text);
    }

    @Override
    protected void onPause() {
        super.onPause();
        plantProgressAutoUpdate.stop();
        mapProgressAutoUpdate.stop();
    }

    private void update() {
        apiRadioGroup.check(API.instance().radioButtonId());
        apiRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (API api : API.all()) {
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
        synchronizePermissionSetting(R.id.toggle_vibration, R.id.toggle_vibration_ask, getPermissions().VIBRATE);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_mundraub, Settings.API_ID_MUNDRAUB);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_na_ovoce, Settings.API_ID_NA_OVOCE);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_fruitmap, Settings.API_ID_FRUITMAP);
        synchronizeCategoryCheckbutton(R.id.checkBox_markers_community, Settings.API_ID_COMMUNITY);
        synchronizeMarkerDownloadCheckbutton(R.id.checkBox_download_mundraub_markers, Settings.API_ID_MUNDRAUB);
        synchronizeMarkerDownloadCheckbutton(R.id.checkBox_download_na_ovoce_markers, Settings.API_ID_NA_OVOCE);
        synchronizeMarkerDownloadCheckbutton(R.id.checkBox_download_fruitmap_markers, Settings.API_ID_FRUITMAP);

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

        synchronizeOfflineMapCheckbutton(R.id.checkBox_offline_mapnik, Settings.TILES_OSM);
        synchronizeOfflineMapCheckbutton(R.id.checkBox_offline_satellite, Settings.TILES_SATELLITE);

    }

    private void synchronizeOfflineMapCheckbutton(final int resourceId, final String mapId) {
        synchronizeCheckbutton(resourceId, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                int result = Settings.setDownloadMap(mapId, checked);
                setOfflineMapStatisticsText();
                return result;
            }
            @Override
            public boolean isChecked() {
                return Settings.getDownloadMap(mapId);
            }
        });
    }

    private void synchronizeMarkerDownloadCheckbutton(int checkboxId, final String apiId) {
        synchronizeCheckbutton(checkboxId, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.downloadMarkersFromAPI(apiId, checked);
            }

            @Override
            public boolean isChecked() {
                return Settings.downloadMarkersFromAPI(apiId);
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
    protected void menuOpenSettings() {
        // do nothing
    }
}
