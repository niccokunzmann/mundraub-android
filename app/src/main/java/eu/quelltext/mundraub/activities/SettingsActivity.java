package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class SettingsActivity extends AppCompatActivity {

    private ToggleButton toggleAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toggleAPI = (ToggleButton) findViewById(R.id.toggle_API);

        toggleAPI.setChecked(Settings.isUsingTheMundraubAPI());
        toggleAPI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Settings.useTheMundraubAPI();
                } else {
                    Settings.useTheDummyAPI();
                }
            }
        });
    }
}
