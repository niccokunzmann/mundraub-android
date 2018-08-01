package eu.quelltext.mundraub;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;

public class NewPlantActivity extends AppCompatActivity {

    private Button buttonPlantType;
    private NewPlantActivity me = this;
    private final int ChoosePlantTypeReturnCode = 0;
    private PlantCategory plantCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_plant);

        buttonPlantType = (Button) findViewById(R.id.button_plant_type);
        buttonPlantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // form https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intent = new Intent(me, ChoosePlantType.class);
                // from https://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android#947560
                startActivityForResult(intent, ChoosePlantTypeReturnCode);
            }
        });
    }
    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // check that it is the SecondActivity with an OK result
        if (requestCode == ChoosePlantTypeReturnCode) {
            if (resultCode == RESULT_OK) {
                this.setPlantCategory(PlantCategory.fromIntent(intent));
            }
        }
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plantCategory = plantCategory;
        Log.d("NewPlantActivity", "Set plant category to " + plantCategory.getTitle());
        this.buttonPlantType.setText(plantCategory.getResourceId());
    }
}
