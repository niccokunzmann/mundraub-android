package eu.quelltext.mundraub.api.progress;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.api.API;

public class JoinedProgress extends Progress  {

    private final List<ProgressableResult> progressables = new ArrayList<ProgressableResult>();
    private int successCount = 0;

    public JoinedProgress(API.Callback callback) {
        super(callback);
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
        return progressables.size();
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
