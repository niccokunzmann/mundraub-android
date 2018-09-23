package eu.quelltext.mundraub.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ProgressBar;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.plant.Plant;

public class StartupActivity extends MundraubBaseActivity {

    private ProgressBar progressBar;
    private AsyncTask<Void, Void, Void> task;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        setTitle(R.string.title_startup);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setProgress(10);

        task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                Plant.loadAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                loaded = true;
                openMap();
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loaded) {
            finish();
        }
    }
}
