package eu.quelltext.mundraub.api;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.plant.Plant;
import okhttp3.ResponseBody;

public abstract class API extends AsyncNetworkInteraction implements BackgroundDownloadTask.DownloadProvider {

    public static final API DUMMY = new DummyAPI();
    public static final API MUNDRAUB = new MundraubAPI();
    public static final API NA_OVOCE = new NaOvoceAPI();
    public static final API FRUITMAP = new FruitMapAPI();
    public static final API DEFAULT = MUNDRAUB;

    private boolean isLoggedIn;

    public static API[] all() {
        Set<String> dowloads = Settings.getCustomNaOvoceDownloads();
        API apis[] = new API[5 + dowloads.size()];
        apis[0] = DUMMY;
        apis[1] = MUNDRAUB;
        apis[2] = FRUITMAP;
        apis[3] = NA_OVOCE;
        apis[4] = new CustomNaOvoceAPI(Settings.getCustomNaOvoceHost(), false);
        int i = 5;
        for (Iterator<String> it = dowloads.iterator(); it.hasNext(); i++) {
            String url = it.next();
            apis[i] = new CustomNaOvoceAPI(url, true);
        }
        return apis;
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

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public static List<API> getMarkerAPIs() {
        List<API> result = new ArrayList<>();
        for (API api: all()) {
            if (api.wantsToProvideMarkers()) {
                result.add(api);
            }
        }
        return result;
    }

    public boolean wantsToProvideMarkers() {
        return Settings.downloadMarkersFromAPI(id());
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


    public Progress addPlant(final Plant plant, Callback callback) {
        return doAsynchronously(callback, new AsyncOperation() {
            @Override
            public int operate(Progress progress) throws ErrorWithExplanation {
                checkPlantForAPI(plant);
                return addPlantAsync(plant);
            }
        });
    }

    protected void checkPlantForAPI(Plant plant) throws ErrorWithExplanation {
        if (plant.getCategory().isUnknown()) {
            abortOperation(R.string.error_plant_category_is_not_set);
        }
        if (!plant.getCategory().canBeUsedByAPI(this)) {
            abortOperation(R.string.error_invalid_category_for_api);
        }
        if (!plant.getPosition().isValid()) {
            abortOperation(R.string.error_plant_position_is_invalid);
        }
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
                checkPlantForAPI(plant);
                return updatePlantAsync(plant, id);
            }
        });
    }

    public boolean canUpdate() {
        return true;
    }

    @Override
    public void handleContent(ResponseBody body, Progressable fraction) throws IOException, ErrorWithExplanation, JSONException {
        String data = body.string();
        log.d("data", data.substring(0, (data.length() > 100 ? 100 : data.length())) + " " + data.length() + " bytes");
        addMarkers(data, fraction);

    }

    @Override
    public double getDownloadFraction() {
        return 0.1;
    }

    @Override
    public Set<String> getDownloadUrls() {
        return getUrlsForAllPlants();
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
    public abstract int nameResourceId();

    public boolean isCustomNaOvoceAPI() {
        return false;
    }
}
