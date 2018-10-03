package eu.quelltext.mundraub.api;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import okhttp3.ResponseBody;

public class BackgroundDownloadTask extends AsyncNetworkInteraction {

    public interface DownloadProvider {
        Set<String> getDownloadUrls();
        void handleContent(ResponseBody body, Progressable fraction) throws IOException, ErrorWithExplanation, JSONException;
        double getDownloadFraction();
    }

    private Progress progress = null;
    private List<DownloadTask> tasks = new ArrayList<>();

    public BackgroundDownloadTask() {
        super();
    }

    private class DownloadTask {

        private final String url;
        private final DownloadProvider downloadProvider;

        public DownloadTask(String url, DownloadProvider downloadProvider) {
            this.url = url;
            this.downloadProvider = downloadProvider;
        }

        public void download(Progressable fraction) throws IOException, ErrorWithExplanation, JSONException {
            ResponseBody body = httpGetBody(url);
            double downloadFraction = downloadProvider.getDownloadFraction();
            fraction.setProgress(downloadFraction);
            downloadProvider.handleContent(body, fraction.getFraction(1 - downloadFraction));
            fraction.setProgress(1);
        }
    }

    public boolean downloadInBackground(Callback callback) {
        if (downloadStarted()) {
            return false;
        }
        progress = doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return downloadAsync(progress);
            }
        });
        return true;
    }

    private int downloadAsync(Progress progress) {
        int error = TASK_SUCCEEDED;
        for (DownloadTask task : tasks) {
            try {
                Progressable fraction = progress.getFraction(1.0 / tasks.size());
                task.download(fraction);
                fraction.setProgress(1);
            } catch (JSONException e) {
                log.printStackTrace(e);
                error = R.string.error_invalid_json_for_markers;
            } catch (Exception e) {
                error = handleExceptionConsistently(e);
            }
        }
        return error;
    }

    public boolean downloadStarted() {
        return progress != null;
    }

    public Progress getProgress() {
        return progress;
    }

    public boolean collectDownloadsFrom(DownloadProvider downloadProvider) {
        if (downloadStarted()) {
            return false;
        }
        for (String url: downloadProvider.getDownloadUrls()) {
            tasks.add(new DownloadTask(url, downloadProvider));
        }
        return true;
    }

}
