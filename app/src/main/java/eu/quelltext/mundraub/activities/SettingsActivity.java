package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.MundraubBaseActivity;
import eu.quelltext.mundraub.initialization.Permissions;

public class SettingsActivity extends MundraubBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }
    private void update() {
        setToggle(R.id.toggle_API,  new Toggled() {
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
        setToggle(R.id.toggle_secure_connection, new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.useInsecureConnections(!checked);
            }

            @Override
            public boolean isChecked() {
                return !Settings.useInsecureConnections();
            }
        });
        setToggle(R.id.toggle_cache, new Toggled() {
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
        updatePermissions(R.id.toggle_camera, R.id.toggle_camera_ask, getPermissions().CAMERA);
        updatePermissions(R.id.toggle_location, R.id.toggle_location_ask, getPermissions().ACCESS_FINE_LOCATION);
        updatePermissions(R.id.toggle_internet, R.id.toggle_internet_ask, getPermissions().INTERNET);
        updatePermissions(R.id.toggle_storage, R.id.toggle_storage_ask, getPermissions().WRITE_EXTERNAL_STORAGE);
    }

    interface Toggled {
        int onToggle(boolean checked);
        boolean isChecked();
    }

    private void setToggle(final int resourceId, final Toggled onToggle) {
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

    void updatePermissions(int onOffId, int askId, final Permissions.Permission permission) {
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
}
