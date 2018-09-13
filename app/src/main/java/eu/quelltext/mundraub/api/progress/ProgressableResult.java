package eu.quelltext.mundraub.api.progress;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.api.API;

public abstract class ProgressableResult extends Progressable {
    private final List<API.Callback> callbacks = new ArrayList<API.Callback>();
    abstract boolean isDone();
    abstract boolean isDoneAndError();
    abstract boolean isDoneAndSuccess();
    abstract int errorResourceId();
    public void addCallback(API.Callback callback) {
        callbacks.add(callback);
        if (isDoneAndError()) {
            callback.onFailure(errorResourceId());
        } else if (isDoneAndSuccess()) {
            callback.onSuccess();
        }
    }
    protected List<API.Callback> getCallbacks() {
        return callbacks;
    }

}
