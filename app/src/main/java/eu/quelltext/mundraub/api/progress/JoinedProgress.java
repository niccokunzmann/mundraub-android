package eu.quelltext.mundraub.api.progress;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.api.API;

public class JoinedProgress extends Progress  {

    private final List<ProgressableResult> progressables = new ArrayList<ProgressableResult>();
    private final int length;
    private int successCount = 0;

    public JoinedProgress(API.Callback callback, int length) {
        super(callback);
        this.length = length;
    }

    public void addProgressable(ProgressableResult progressableResult) {
        progressables.add(progressableResult);
        progressableResult.addCallback(new API.Callback() {
            @Override
            public void onSuccess() {
                successCount += 1;
                if (successCount == size() && !isDone()) {
                    setSuccess();
                }
            }

            @Override
            public void onFailure(int errorResourceString) {
                if (!isDone()) {
                    setError(errorResourceString);
                }
            }
        });
    }

    private int size() {
        return length;
    }

    @Override
    public double getProgress() {
        double sum = 0;
        for (ProgressableResult p: progressables) {
            sum += p.getProgress();
        }
        return sum / size();
    }
}
