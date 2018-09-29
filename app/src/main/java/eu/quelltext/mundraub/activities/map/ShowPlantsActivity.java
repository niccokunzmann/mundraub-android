package eu.quelltext.mundraub.activities.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.NewPlantActivity;

public class ShowPlantsActivity extends MapBaseActivity {

    public static final String ARG_POSITION = "position_longitude_latitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_plants);
        initializeWebView(R.id.web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowPlantsActivity.this, NewPlantActivity.class);
                String url = webView.getUrl();
                intent.putExtra(NewPlantActivity.ARG_PLANT_LOCATION_MAP_URL, url);
                startActivity(intent);
            }
        });
        openMapAtBestPosition();
    }

    private void openMapAtBestPosition() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(ARG_POSITION)) {
            double[] position = extras.getDoubleArray(ARG_POSITION);
            openMapAtPosition(position);
        } else {
            openMapAtLastPlantOrDefault();
        }
    }

}
