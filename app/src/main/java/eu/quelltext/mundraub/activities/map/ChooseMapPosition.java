package eu.quelltext.mundraub.activities.map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.plant.Plant;

public class ChooseMapPosition extends MapBaseActivity {

    public static final String ARG_PLANT_ID = "plant_id";
    private Plant plant;
    private Button cancelButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_map_position);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_PLANT_ID)) {
            // load saved state first
            plant = Plant.withId(savedInstanceState.getString(ARG_PLANT_ID));
        } else if (extras != null && extras.containsKey(ARG_PLANT_ID)) {
            // load plant from arguments
            plant = Plant.withId(getIntent().getStringExtra(ARG_PLANT_ID));
        } else {
            log.e("ChooseMapPosition","No plant was specified.");
            finish();
        }
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
                plant.setPositionFromMapUrl(getUrl());
                finish();
            }
        });
        initializeWebView(R.id.map_view);
        setPositionToPlant();
    }

    void setPositionToPlant() {
        Plant.Position position = plant.getBestPositionForMap();
        if (position != plant.getPosition()) {
            alertAboutPositionGuess();
        }
        openMapAtPosition(position);
    }

    private void alertAboutPositionGuess() {
        new Dialog(this).alertInfo(R.string.info_plant_position_is_chosen_from_best_guess);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
        outState.putString(ARG_PLANT_ID, plant.getId());
    }

}
