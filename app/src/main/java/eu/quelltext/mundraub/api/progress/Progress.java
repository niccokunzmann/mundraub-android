package eu.quelltext.mundraub.api.progress;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.api.API;

public class Progress extends Progressable {

    private final List<API.Callback> callbacks = new ArrayList<API.Callback>();
    private boolean done = false;
    private boolean error = false;
    private int errorResourceId;
    private double progress = 0;

    public Progress(API.Callback callback) {
        super();
        callbacks.add(callback);
    }

    public boolean isDone() {
        return done;
    }

    public boolean isDoneAndError() {
        return done && error;
    }

    public boolean isDoneAndSuccess() {
        return done && !error;
    }

    public int errorResourceId() {
        return errorResourceId;
    }

    public void setError(int errorResourceId) {
        if (done) {
            log.e("setError", "cannot complete twice");
            return;
        }
        this.errorResourceId = errorResourceId;
        error = true;
        done = true; // should be last because of concurrency
        progress = 1;
        for (API.Callback callback : callbacks) {
            callback.onProgress(progress);
            callback.onFailure(errorResourceId);
        }
    }

    public void setSuccess() {
        if (done) {
            log.e("setError", "cannot complete twice");
            return;
        }
        error = false;
        done = true; // should be last because of concurrency
        progress = 1;
        for (API.Callback callback : callbacks) {
            callback.onProgress(progress);
            callback.onSuccess();
        }
    }

    public void setProgress(double portion) {
        if (portion > 1) {
            progress = 1;
        } else if (portion < 0) {
            progress = 0;
        } else {
            progress = portion;
        }
        for (API.Callback callback : callbacks) {
            callback.onProgress(progress);
        }
    }

    public double getProgress() {
        return progress;
    }

    public void addCallback(API.Callback callback) {
        callbacks.add(callback);
    }
}