package eu.quelltext.mundraub.activities.map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.notification.FruitRadarNotification;
import eu.quelltext.mundraub.map.MapUrl;

public class TestFruitRadarActivity extends MapBaseActivity {

    private Timer timer;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_fruit_radar);
        initializeWebView(R.id.web_view);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        openMapAtLastPlantOrDefault();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // schedule task every seconds from https://stackoverflow.com/a/9406880
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MapUrl url = getUrl();
                FruitRadarNotification.testLocation(url.getLongitude(), url.getLatitude());
            }

        },0,1000);//Update text every second
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer = null;
    }
}
