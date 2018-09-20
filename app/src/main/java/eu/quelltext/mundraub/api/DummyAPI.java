package eu.quelltext.mundraub.api;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.plant.Plant;

public class DummyAPI extends API {

    private int id = 1;

    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
        simulateNetwork();
        if (username.equals("test") && password.equals("test") || username.equals("apptest")) {
            return TASK_SUCCEEDED;
        } else {
            return R.string.invalid_credentials;
        }
    }

    @Override
    protected int signupAsync(String email, String username, String password) throws ErrorWithExplanation {
        simulateNetwork();
        return TASK_SUCCEEDED;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        simulateNetwork();
        return TASK_SUCCEEDED;
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        simulateNetwork();
        return TASK_SUCCEEDED;
    }

    @Override
    protected Set<String> getUrlsForAllPlants() {
        return new HashSet<String>();
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {

    }

    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        simulateNetwork();
        plant.online().publishedWithId(Integer.toString(id++), this);
        return TASK_SUCCEEDED;
    }

    private int simulateNetwork() throws ErrorWithExplanation {
        try {
            // Simulate network access.
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            abortOperation(TASK_CANCELLED);
        }
        return TASK_SUCCEEDED;
    }

    @Override
    public String id() {
        return Settings.API_ID_DUMMY;
    }

    public int radioButtonId(){
        return R.id.radioButton_dummy;
    }

    public String getPlantUrl(String id) {
        return null;
    }

    @Override
    public int nameResourceId() {
        return R.string.login_api_name_dummy;
    }
}
