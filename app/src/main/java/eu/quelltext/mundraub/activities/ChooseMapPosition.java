package eu.quelltext.mundraub.activities;

import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.IOException;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.MundraubBaseActivity;
import eu.quelltext.mundraub.map.MundraubProxy;
import eu.quelltext.mundraub.plant.Plant;

public class ChooseMapPosition extends MundraubBaseActivity {

    public static final String ARG_PLANT_ID = "plant_id";
    private Plant plant;
    private Button cancelButton;
    private Button saveButton;
    private WebView webView;
    private MundraubProxy apiProxy;

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
        webView = (WebView) findViewById(R.id.map_view);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // from https://stackoverflow.com/a/8846105/1320237
        // from https://stackoverflow.com/a/32587047/1320237
        //webSettings.setBuiltInZoomControls(true);
        //webSettings.setDisplayZoomControls(false);
        // from https://stackoverflow.com/a/6255353/1320237
        //webView.setVerticalScrollBarEnabled(true);
        //webView.setHorizontalScrollBarEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // from https://stackoverflow.com/a/30294054/1320237
                log.d("WebView", consoleMessage.sourceId() + " at line " +
                        consoleMessage.lineNumber() + ": " + consoleMessage.message());
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // TODO: check for API level 21? https://stackoverflow.com/a/47913011
                CookieSyncManager.getInstance().sync(); // from https://stackoverflow.com/a/8390280
            }
        });

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
        apiProxy = Settings.getMundraubMapProxy();
        setPositionToPlant();
    }

    void setPositionToPlant() {
        Plant.Position position = plant.getBestPositionForMap();
        if (position != plant.getPosition()) {
            alertAboutPositionGuess();
        }
        String url = position.getMapURLWithMarker();
        log.d("ChooseMapPosition", "set url to " + url);
        webView.loadUrl(url);
        webView.reload();
    }

    private void alertAboutPositionGuess() {
        new Dialog(this).alertInfo(R.string.info_plant_position_is_chosen_from_best_guess);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
        outState.putString(ARG_PLANT_ID, plant.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            log.d("start", "proxy1");
            apiProxy.start();
            log.d("start", "proxy2");
        } catch (IOException e) {
            log.printStackTrace(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        apiProxy.stop();
    }
}
