package eu.quelltext.mundraub.api;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.map.PlantsCache;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;

public class NaOvoceAPI extends API {
    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
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
        HashSet<String> urls = new HashSet<String>();
        for (PlantCategory category: PlantCategory.all()) {
            // see https://github.com/niccokunzmann/mundraub-android/issues/96
            urls.add("https://na-ovoce.cz/api/v1/fruit/?kind=" + category.getValueForNaOvoceAPI());
        }
        return urls;
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {
        JSONArray json = new JSONArray(data);
        PlantsCache.updateNaOvocePlantMarkers(json, fraction);
    }

}
