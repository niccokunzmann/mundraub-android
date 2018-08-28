package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToggle(R.id.toggle_API, Settings.useMundraubAPI(),  new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.useMundraubAPI(checked);
            }
        });
        setToggle(R.id.toggle_secure_connection, !Settings.useInsecureConnections(),  new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.useInsecureConnections(!checked);
            }
        });
        setToggle(R.id.toggle_cache, Settings.useCacheForPlants(), new Toggled() {
            @Override
            public int onToggle(boolean checked) {
                return Settings.useCacheForPlants(checked);
            }
        });
    }

    interface Toggled {
        int onToggle(boolean checked);
    }

    private void setToggle(final int resourceId, boolean usingTheMundraubAPI, final Toggled onToggle) {
        final ToggleButton toggle = (ToggleButton) findViewById(resourceId);
        toggle.setChecked(usingTheMundraubAPI);
        final SettingsActivity me = this;
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int result = onToggle.onToggle(isChecked);
                if (result != Settings.COMMIT_SUCCESSFUL) {
                    toggle.toggle();
                    new Dialog(me).alertError(resourceId);
                }
            }
        });
    }
}
