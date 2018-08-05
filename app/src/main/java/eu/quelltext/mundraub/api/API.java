package eu.quelltext.mundraub.api;

import android.os.AsyncTask;

public abstract class API {

    private static final API dummyAPI = new DummyAPI();
    private static final API mundraubAPI = new MundraubAPI();
    private static final boolean useDummy = false; // for debug purposes use dummy api

    public static final API instance() {
        if (useDummy) {
            return dummyAPI;
        }
        return mundraubAPI;
    }

    private boolean isLoggedIn = false;

    public void login(String username, String password, LoginCallback cb) {
        UserLoginTask task = new UserLoginTask(username, password, cb);
        task.execute((Void) null);
    }

    abstract boolean login(String username, String password);

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;
        private final LoginCallback cb;

        UserLoginTask(String username, String password, LoginCallback cb) {
            this.username = username;
            this.password = password;
            this.cb = cb;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return login(username, password);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            isLoggedIn = success;
            if (isLoggedIn()) {
                cb.onSuccess();
            } else {
                cb.onFailure();
            }
        }

        @Override
        protected void onCancelled() {
            cb.onFailure();
        }
    }

    public interface LoginCallback {
        void onSuccess();
        void onFailure();
    }

}
