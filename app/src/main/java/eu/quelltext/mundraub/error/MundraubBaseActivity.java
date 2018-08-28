package eu.quelltext.mundraub.error;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.AboutActivity;
import eu.quelltext.mundraub.activities.SettingsActivity;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.initialization.Permissions;

public class MundraubBaseActivity extends AppCompatActivity implements Logger.Loggable {

    protected Logger.Log log;
    private Permissions permissions = null;

    protected Permissions getPermissions() {
        if (permissions == null) {
            permissions = Permissions.of(this);
        }
        return permissions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Initialization.provideActivity(this);
        log = Logger.newFor(this);
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
            case R.id.item_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getPermissions().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
