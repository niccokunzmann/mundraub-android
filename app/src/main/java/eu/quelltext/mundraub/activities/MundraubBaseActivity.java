package eu.quelltext.mundraub.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.map.ShowPlantsActivity;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.initialization.Permissions;

public class MundraubBaseActivity extends AppCompatActivity implements Logger.Loggable {

    protected Logger.Log log;
    private Permissions permissions = null;
    private boolean isCreated = false;
    private boolean isStarted = false;
    private boolean isResumed = false;

    protected Permissions getPermissions() {
        if (permissions == null) {
            permissions = Permissions.of(this);
        }
        return permissions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApp();
        log = Logger.newFor(this);
        isCreated = true;
        log.d("activity life cycle", "onCreate");
    }

    protected void initializeApp() {
        Initialization.provideActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreated = false;
        log.d("activity life cycle", "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStarted = true;
        log.d("activity life cycle", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStarted = false;
        log.d("activity life cycle", "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        log.d("activity life cycle", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
        log.d("activity life cycle", "onPause");
    }

/*    public boolean isCreated() {
        return isCreated;
    }

    public boolean isResumed() {
        return isResumed;
    }*/

    public boolean canCreateDialog() {
        return isCreated;
    }

    @Override
    public String getTag() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // from https://www.javatpoint.com/android-option-menu-example
        switch (item.getItemId()){
            case R.id.item_about:
                openAbout();
                return true;
            case R.id.item_map:
                openMap();
                return true;
            case R.id.item_settings:
                openSettings();
                return true;
            case R.id.item_my_plants:
                openMyPlants();
                return true;
            case R.id.item_rules:
                openMundraubRules();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void openMyPlants() {
        Intent intent = new Intent(this, PlantListActivity.class);
        this.startActivity(intent);
    }

    protected void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    protected void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        this.startActivity(intent);
    }

    protected void openMap() {
        Intent intent = new Intent(this, ShowPlantsActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getPermissions().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void openURLInBrowser(String url) {
        // from https://stackoverflow.com/a/3004542/1320237
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    protected void openMundraubRules() {
        Intent intent = new Intent(this, CodexActivity.class);
        startActivity(intent);
    }

    public LocationManager createLocationManager() {
        getPermissions().ACCESS_FINE_LOCATION.askIfNotGranted();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // from https://stackoverflow.com/a/10917500
        return (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
    }
}
