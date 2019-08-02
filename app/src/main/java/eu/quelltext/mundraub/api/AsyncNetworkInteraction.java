package eu.quelltext.mundraub.api;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.ErrorAware;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/* This class is meant to be used as a subclass.
 *
 */
public class AsyncNetworkInteraction extends ErrorAware {
    public final int TASK_SUCCEEDED = R.string.task_completed_successfully;
    public final int TASK_CANCELLED = R.string.task_was_cancelled;


    protected class Task extends AsyncTask<Void, Void, Integer> {

        private final Progress progress;
        private final API.AsyncOperation operation;

        Task(API.Callback cb, API.AsyncOperation operation) {
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

    protected abstract class AsyncOperation {
        protected abstract int operate(Progress progress) throws ErrorWithExplanation;
    }

    public static class ErrorWithExplanation extends Exception {
        private final int explanationResourceId;

        public ErrorWithExplanation(int explanationResourceId) {
            super();
            this.explanationResourceId = explanationResourceId;
        }

        public int getExplanationResourceId() {
            return explanationResourceId;
        }
    }

    protected int handleExceptionConsistently(Exception e) {
        log.printStackTrace(e);
        return errorToExplanation(e);
    }

    public static int errorToExplanation(Exception e) {
        if (SSLHandshakeException.class.isInstance(e)) {
            return R.string.error_could_not_validate_host;
        } else if (MalformedURLException.class.isInstance(e)) {
            return R.string.error_malformed_url;
        } else if (UnknownHostException.class.isInstance(e)) {
            return R.string.error_unknown_hostname;
        } else if (ProtocolException.class.isInstance(e)) {
            return R.string.error_invalid_protocol;
        } else if (SSLProtocolException.class.isInstance(e)) {
            return R.string.error_could_not_establish_a_secure_connection;
        } else if (RuntimeException.class.isInstance(e)) {
            if (NoSuchAlgorithmException.class.isInstance(e.getCause())) {
                return R.string.error_no_such_algorithm;
            }
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

    protected ResponseBody httpGetBody(String url) throws IOException, ErrorWithExplanation {
        OkHttpClient client = Settings.getOkHttpClient(getSSLInstanceName());
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
        return response.body();
    }

    protected String httpGet(String url) throws IOException, ErrorWithExplanation {
        return httpGetBody(url).string();
    }


    public static void abortOperation(int resourceId) throws ErrorWithExplanation {
        throw new ErrorWithExplanation(resourceId);
    }

    protected String getSSLInstanceName() {
        return "SSL";
    }


    protected Progress doAsynchronously(Callback cb, AsyncOperation op) {
        Task task = new Task(cb, op);
        task.execute();
        return task.getProgress();
    }

}
