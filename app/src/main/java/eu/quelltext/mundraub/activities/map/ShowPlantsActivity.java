package eu.quelltext.mundraub.activities.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.NewPlantActivity;
import eu.quelltext.mundraub.initialization.Permissions;

public class ShowPlantsActivity extends MapBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_plants);
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
        initializeWebView(R.id.web_view);
        setMapToBestPosition();
    }

    private void setMapToGPSPosition() {

    }

    private void setMapToBestPosition() {
        webView.loadUrl("file:///android_asset/map/examples/fullScreen.html?8.559300000000329,51.97691767671171");
        getPermissions().ACCESS_FINE_LOCATION.askIfNotGranted(new Permissions.PermissionChange() {
            @Override
            public void onGranted(Permissions.Permission permission) {
                setMapToGPSPosition();
            }

            @Override
            public void onDenied(Permissions.Permission permission) {}
        });
    }
}
