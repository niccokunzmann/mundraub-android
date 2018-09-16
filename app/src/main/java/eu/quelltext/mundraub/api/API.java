package eu.quelltext.mundraub.api;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.plant.Plant;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public abstract class API extends ErrorAware {

    public static final API DUMMY = new DummyAPI();
    public static final API MUNDRAUB = new MundraubAPI();
    public static final API NA_OVOCE = new NaOvoceAPI();
    public static final API DEFAULT = MUNDRAUB;
    public final int TASK_SUCCEEDED = R.string.task_completed_successfully;
    public final int TASK_CANCELLED = R.string.task_was_cancelled;

    private boolean isLoggedIn;

    public static API[] all() {
        return new API[]{DUMMY, MUNDRAUB, NA_OVOCE};
    }
    
    public static API instance() {
        return fromId(Settings.getAPIId());
    }

    public Progress login(final String username, final String password, Callback cb) {
        return doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                int success = loginAsync(username, password);
                isLoggedIn = success == TASK_SUCCEEDED;
                return success;
            }
        });
    }

    private Progress doAsynchronously(Callback cb, AsyncOperation op) {
        Task task = new Task(cb, op);
        task.execute();
        return task.getProgress();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public static API[] getMarkerAPIs() {
        return new API[]{
                MUNDRAUB,
                NA_OVOCE
        }; // TODO: settings
    }

    public Progress signup(final String email, final String username, final String password, Callback callback) {
        return doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return signupAsync(email, username, password);
            }
        });
    }

    public static API fromId(String id) {
        for (API api: all()) {
            if (api.id().equals(id)) {
                return api;
            }
        }
        return DEFAULT;
    }

    private class Task extends AsyncTask<Void, Void, Integer> {

        private final Progress progress;
        private final AsyncOperation operation;

        Task(Callback cb, AsyncOperation operation) {
            this.operation = operation;
            this.progress = new Progress(cb);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return operation.operate(progress);
            } catch (ErrorWithExplanation e) {
                return e.explanationResourceId;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == TASK_SUCCEEDED) {
                progress.setSuccess();
            } else {
                progress.setError(result);
            }
        }

        @Override
        protected void onCancelled() {
            progress.setError(TASK_CANCELLED);
        }

        public Progress getProgress() {
            return progress;
        }
    }

    public static abstract class Callback {
        public static final Callback NULL = new Callback() {
            @Override
            public void onSuccess() {
            }
        };

        public abstract void onSuccess();
        public void onFailure(int errorResourceString){};
        public void onProgress(double portion){};
    }
    private abstract class AsyncOperation {
        abstract int operate(Progress progress) throws ErrorWithExplanation;
    }

    public Progress addPlant(final Plant plant, Callback callback) {
        return doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return addPlantAsync(plant);
            }
        });
    }

    public Progress deletePlant(final String id, Callback callback) {
        return doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return deletePlantAsync(id);
            }
        });
    }

    public Progress updatePlant(final Plant plant, final String id, Callback cb) {
        return doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return updatePlantAsync(plant, id);
            }
        });
    }

    public Progress updateAllPlantMarkers(Callback cb) {
        return doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                return setAllPlantMarkersAsync(progress);
            }
        });
    }

    public static class ErrorWithExplanation extends Exception {
        private final int explanationResourceId;

        private ErrorWithExplanation(int explanationResourceId) {
            super();
            this.explanationResourceId = explanationResourceId;
        }

        public int getExplanationResourceId() {
            return explanationResourceId;
        }
    }

    public static void abortOperation(int resourceId) throws ErrorWithExplanation {
        throw new ErrorWithExplanation(resourceId);
    }

    protected int setAllPlantMarkersAsync(Progressable progress) throws ErrorWithExplanation {
        double FRACTION_DOWNLOAD = 0.05;
        double FRACTION_PARSING = 1 - FRACTION_DOWNLOAD;
        Set<String> urls = getUrlsForAllPlants();
        for (String url : urls) {
        try {
                log.d("setAllPlantMarkersAsync", url);
                String data = httpGet(url);
                progress.getFraction(FRACTION_DOWNLOAD / urls.size()).setProgress(1);
                log.d("data", data.substring(0, (data.length() > 100 ? 100 : data.length())) + " " + data.length() + " bytes");
                Progressable fraction = progress.getFraction(FRACTION_PARSING / urls.size());
                addMarkers(data, fraction);
                fraction.setProgress(1);
            } catch (JSONException e) {
                log.printStackTrace(e);
                return R.string.error_invalid_json_for_markers;
            } catch (Exception e) {
                return handleExceptionConsistently(e);
            }
        }
        return TASK_SUCCEEDED;
    }

    protected String httpGet(String url) throws IOException, ErrorWithExplanation {
        OkHttpClient client = Settings.getOkHttpClient();
        log.d("API GET", url.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .method("GET", null)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        int code = response.code();
        if (code != 200) {
            abortOperation(R.string.error_unexpected_return_code);
        }
        return response.body().string();
    }

    protected int handleExceptionConsistently(Exception e) {
        log.printStackTrace(e);
        if (SSLHandshakeException.class.isInstance(e)) {
            return R.string.error_could_not_validate_host;
        } else if (MalformedURLException.class.isInstance(e)) {
            return R.string.error_malformed_url;
        } else if (UnknownHostException.class.isInstance(e)) {
            return R.string.error_unknown_hostname;
        } else if (ProtocolException.class.isInstance(e)) {
            return R.string.error_invalid_protocol;
        } else if (NoSuchAlgorithmException.class.isInstance(e)) {
            return R.string.error_no_such_algorithm;
        } else if (KeyManagementException.class.isInstance(e)) {
            return R.string.error_key_management;
        } else if (SocketTimeoutException.class.isInstance(e)) {
            return R.string.error_timeout;
        } else if (IOException.class.isInstance(e)) {
            return R.string.error_connection;
        } else if (ErrorWithExplanation.class.isInstance(e)) {
            return ((ErrorWithExplanation)e).getExplanationResourceId();
        }
        return R.string.error_not_specified;
    }

    // methods to replace

    protected abstract int addPlantAsync(Plant plant) throws ErrorWithExplanation;
    protected abstract int loginAsync(String username, String password) throws ErrorWithExplanation;
    protected abstract int signupAsync(String email, String username, String password) throws ErrorWithExplanation;
    protected abstract int deletePlantAsync(String plantId) throws ErrorWithExplanation;
    protected abstract int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation;
    protected abstract Set<String> getUrlsForAllPlants();
    protected abstract void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation;
    public abstract String id();
    public abstract int radioButtonId();
    public abstract String getPlantUrl(String id);
    public abstract int nameResourceIdForLoginActivity();


}
