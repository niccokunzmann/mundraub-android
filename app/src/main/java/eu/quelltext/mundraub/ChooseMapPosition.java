package eu.quelltext.mundraub;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
        // from https://stackoverflow.com/a/32587047/1320237
        //webSettings.setBuiltInZoomControls(true);
        //webSettings.setDisplayZoomControls(false);
        // from https://stackoverflow.com/a/6255353/1320237
        //webView.setVerticalScrollBarEnabled(true);
        //webView.setHorizontalScrollBarEnabled(true);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = webView.getUrl();
                plant.setPositionFromMapUrl(url);
                finish();
            }
        });
        setPositionToPlant();
    }

    void setPositionToPlant() {
        Plant.Position position = plant.getBestPositionForMap();
        if (position != plant.getPosition()) {
            alertAboutPositionGuess();
        }
        String url = position.getMapURLWithMarker();
        Log.d("ChooseMapPosition", "set url to " + url);
        webView.loadUrl(url);
        webView.reload();
    }

    private void alertAboutPositionGuess() {
        // from https://stackoverflow.com/a/2115770/1320237
        AlertDialog.Builder builder = Helper.getAlertBuilder(this);
        builder.setTitle(R.string.attention)
                .setMessage(R.string.info_plant_position_is_chosen_from_best_guess)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
        outState.putString(ARG_PLANT_ID, plant.getId());
    }
}
