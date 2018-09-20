package eu.quelltext.mundraub.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.PlantsCache;
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
        return new HashSet<>(Arrays.asList(
                host() + "?ajax=index&fruit=apple",
                host() + "?ajax=index&fruit=apricot",
                host() + "?ajax=index&fruit=cherry",
                host() + "?ajax=index&fruit=mulberry",
                host() + "?ajax=index&fruit=hazelnut",
                host() + "?ajax=index&fruit=redribes",
                host() + "?ajax=index&fruit=peach",
                host() + "?ajax=index&fruit=gooseberry",
                host() + "?ajax=index&fruit=elderberry",
                host() + "?ajax=index&fruit=josta",
                host() + "?ajax=index&fruit=raspberry",
                host() + "?ajax=index&fruit=briar",
                host() + "?ajax=index&fruit=tangerine",
                host() + "?ajax=index&fruit=almond",
                host() + "?ajax=index&fruit=lemon",
                host() + "?ajax=index&fruit=bilberries",
                host() + "?ajax=index&fruit=chokeberry",
                host() + "?ajax=index&fruit=medlar",
                host() + "?ajax=index&fruit=service-tree",
                host() + "?ajax=index&fruit=blackthorn",
                host() + "?ajax=index&fruit=sea-buckthorn",
                host() + "?ajax=index&fruit=lemon-balm",
                host() + "?ajax=index&fruit=wild-garlic",
                host() + "?ajax=index&fruit=pear",
                host() + "?ajax=index&fruit=plum",
                host() + "?ajax=index&fruit=walnut",
                host() + "?ajax=index&fruit=sourcherry",
                host() + "?ajax=index&fruit=horsechestnut",
                host() + "?ajax=index&fruit=fig",
                host() + "?ajax=index&fruit=strawberries",
                host() + "?ajax=index&fruit=greengage",
                host() + "?ajax=index&fruit=blackribes",
                host() + "?ajax=index&fruit=chestnut",
                host() + "?ajax=index&fruit=blackberry",
                host() + "?ajax=index&fruit=grape",
                host() + "?ajax=index&fruit=quince",
                host() + "?ajax=index&fruit=orange",
                host() + "?ajax=index&fruit=grapefruit",
                host() + "?ajax=index&fruit=dogwood",
                host() + "?ajax=index&fruit=cornel",
                host() + "?ajax=index&fruit=rowan",
                host() + "?ajax=index&fruit=yellow-plum",
                host() + "?ajax=index&fruit=hawthorn",
                host() + "?ajax=index&fruit=silverberry",
                host() + "?ajax=index&fruit=mint",
                host() + "?ajax=index&fruit=wild-strawberry"
        ));
    }

    private String host() {
        if (Settings.useInsecureConnections()) {
            return "http://fruitmap.quelltext.eu/api/";
        }
        return "https://www.fruitmap.org/";
    }

    @Override
    protected String getSSLInstanceName() {
        return "TLSv1";
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {
        int startOfJSON = data.indexOf("{");
        String JSONData = data.substring(startOfJSON);
        JSONObject json = new JSONObject(JSONData);
        PlantsCache.updateFruitMapPlantMarkers(json, fraction);
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
