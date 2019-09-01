package eu.quelltext.mundraub.activities.map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.MapUrl;

public class SelectOfflineMapPartsActivity extends MapBaseActivity {

    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_map_position);
        saveButton = (Button) findViewById(R.id.button_ok);
        cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.setOfflineAreaBoundingBoxes(getUrl().getOfflineAreaBoundingBoxes());
                finish();
            }
        });
        initializeWebView(R.id.map_view);
        openMapAtLastPlantOrDefault();
    }

    @Override
    protected void openMapAt(MapUrl url) {
        super.openMapAt(url.createBoxes());
    }
}
