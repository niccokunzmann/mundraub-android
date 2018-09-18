package eu.quelltext.mundraub.api;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.plant.Plant;

public class FruitMapAPI extends API {
    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int signupAsync(String email, String username, String password) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected Set<String> getUrlsForAllPlants() {
        return new HashSet<>();
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {
    }

    @Override
    public String id() {
        return Settings.API_ID_FRUITMAP;
    }

    @Override
    public int radioButtonId() {
        return R.id.radioButton_fruitmap;
    }

    @Override
    public String getPlantUrl(String id) {
        return null;
    }

    @Override
    public int nameResourceIdForLoginActivity() {
        return R.string.login_api_name_fruitmap;
    }
}
