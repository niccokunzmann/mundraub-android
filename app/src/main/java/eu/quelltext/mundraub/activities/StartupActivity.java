package eu.quelltext.mundraub.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.map.ShowPlantsActivity;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.plant.Plant;

public class StartupActivity extends MundraubBaseActivity {

    private ProgressBar progressBar;
    private AsyncTask<Void, Void, Void> task;
    private boolean loaded = false;
    private int dialogs = 0;
    private boolean nextActivityOpened = false;
    private Class nextActivity = ShowPlantsActivity.class;

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
                openNextActivityIfPossible();
            }
        };
        task.execute();

        if (Settings.shouldAskTheUserToOpenThePrivacyPolicy()) {
            new Dialog(this).askYesNo(
                    R.string.reason_open_privacy_policy,
                    R.string.question_open_privacy_policy,
                    new Dialog.YesNoCallback() {
                        @Override
                        public void yes() {
                            nextActivity = PrivacyPolicyActivity.class;
                        }

                        @Override
                        public void no() {
                            Settings.userDidNotWantToViewThePolicyOnStart();
                        }
                    });
        }
    }

    private void openNextActivityIfPossible() {
        if (loaded) {
            progressBar.setVisibility(View.INVISIBLE); // from https://stackoverflow.com/a/38472059
            if (dialogs == 0 && !nextActivityOpened) {
                nextActivityOpened = true;
                log.d("nextActivity", nextActivity.getSimpleName());
                openActivity(nextActivity);
            }
        }
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

    @Override
    public void onDialogClosed(Dialog dialog) {
        super.onDialogClosed(dialog);
        dialogs--;
        openNextActivityIfPossible();
    }

    @Override
    public void onDialogOpened(Dialog dialog) {
        super.onDialogOpened(dialog);
        dialogs++;
    }
}
