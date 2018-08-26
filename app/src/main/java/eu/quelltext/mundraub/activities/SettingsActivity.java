package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
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
        setToggle(R.id.toggle_API, Settings.useMundraubAPI(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.useMundraubAPI(isChecked);
            }
        });
        setToggle(R.id.toggle_secure_connection, !Settings.useInsecureConnections(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.useInsecureConnections(!isChecked);
            }
        });
    }

    private void setToggle(int resourceId, boolean usingTheMundraubAPI, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        ToggleButton toggle = (ToggleButton) findViewById(resourceId);
        toggle.setChecked(usingTheMundraubAPI);
        toggle.setOnCheckedChangeListener(onCheckedChangeListener);
    }
}
