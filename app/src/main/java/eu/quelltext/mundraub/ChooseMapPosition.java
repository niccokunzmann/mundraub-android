package eu.quelltext.mundraub;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import eu.quelltext.mundraub.plant.Plant;

public class ChooseMapPosition extends AppCompatActivity {

    public static final String ARG_PLANT_ID = "plant_id";
    private Plant plant;
    private Button cancelButton;
    private Button saveButton;
    private WebView webView;

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
            Log.e("ChooseMapPosition","No plant was specified.");
            finish();
        }
        saveButton = (Button) findViewById(R.id.button_ok);
        cancelButton = (Button) findViewById(R.id.button_cancel);
        webView = (WebView) findViewById(R.id.map_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // from https://stackoverflow.com/a/8846105/1320237

        setPositionToPlant();
    }

    void setPositionToPlant() {
        String url = plant.getPosition().getOpenStreetMapWithMarker();
        Log.d("ChooseMapPosition", "set url to " + url);
        webView.loadUrl(url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
        outState.putString(ARG_PLANT_ID, plant.getId());
    }
}
