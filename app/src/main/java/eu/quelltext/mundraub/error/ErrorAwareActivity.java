package eu.quelltext.mundraub.error;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ErrorAwareActivity extends AppCompatActivity implements Logger.Loggable {

    public Logger.Log log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = Logger.newFor(this);
    }

    @Override
    public String getTag() {
        return this.getClass().getSimpleName();
    }
}
