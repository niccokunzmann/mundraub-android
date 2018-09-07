package eu.quelltext.mundraub.api;

import android.os.AsyncTask;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.plant.Plant;

public abstract class API extends ErrorAware {

    private static final API dummyAPI = new DummyAPI();
    private static final API mundraubAPI = new MundraubAPI();
    public final int TASK_SUCCEEDED = R.string.task_completed_successfully;
    public final int TASK_CANCELLED = R.string.task_was_cancelled;

    private boolean isLoggedIn;
    
    public static final API instance() {
        return Settings.useMundraubAPI() ? mundraubAPI : dummyAPI;
    }

    public void login(final String username, final String password, Callback cb) {
        doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate() throws ErrorWithExplanation {
                int success = loginAsync(username, password);
                isLoggedIn = success == TASK_SUCCEEDED;
                return success;
            }
        });
    }

    private void doAsynchronously(Callback cb, AsyncOperation op) {
        Task task = new Task(cb, op);
        task.execute();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    private class Task extends AsyncTask<Void, Void, Integer> {

        private final Callback cb;
        private final AsyncOperation operation;

        Task(Callback cb, AsyncOperation operation) {
            this.operation = operation;
            this.cb = cb;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                return operation.operate();
            } catch (ErrorWithExplanation e) {
                return e.explanationResourceId;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == TASK_SUCCEEDED) {
                cb.onSuccess();
            } else {
                cb.onFailure(result);
            }
        }

        @Override
        protected void onCancelled() {
            cb.onFailure(TASK_CANCELLED);
        }
    }

    public interface Callback {
        void onSuccess();
        void onFailure(int errorResourceString);
    }
    private interface AsyncOperation {
        int operate() throws ErrorWithExplanation;
    }

    public void addPlant(final Plant plant, Callback callback) {
        doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate() throws ErrorWithExplanation {
                return addPlantAsync(plant);
            }
        });
    }

    public void deletePlant(final String id, Callback callback) {
        doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate() throws ErrorWithExplanation {
                return deletePlantAsync(id);
            }
        });
    }

    public void updatePlant(final Plant plant, final String id, Callback cb) {
        doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate() throws ErrorWithExplanation {
                return updatePlantAsync(plant, id);
            }
        });
    }

    public void updateAllPlantMarkers(Callback cb) {
        doAsynchronously(cb, new AsyncOperation() {
            @Override
            public int operate() throws ErrorWithExplanation {
                return setAllPlantMarkersAsync();
            }
        });
    }

    public static class ErrorWithExplanation extends Throwable {
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

    // methods to replace

    protected abstract int addPlantAsync(Plant plant) throws ErrorWithExplanation;
    protected abstract int loginAsync(String username, String password) throws ErrorWithExplanation;
    protected abstract int deletePlantAsync(String plantId) throws ErrorWithExplanation;
    protected abstract int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation;
    protected abstract int setAllPlantMarkersAsync() throws ErrorWithExplanation;

}
