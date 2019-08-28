package eu.quelltext.mundraub.api;

import java.util.HashMap;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class CustomNaOvoceAPI extends NaOvoceAPI {


    private static final String ID_SEPERATOR = " ";
    private final String host;
    private static final HashMap<String, CustomNaOvoceAPI> apis = new HashMap<>();

    public CustomNaOvoceAPI(String host) {
        this.host = host;
    }

    @Override
    protected String host() {
        return host;
    }

    @Override
    public String idForPlant() {
        return Settings.API_ID_MY_NA_OVOCE + ID_SEPERATOR + host();
    }

    @Override
    public String idForCategory () {
        return Settings.API_ID_NA_OVOCE;
    }

    @Override
    public int nameResourceId() {
        return R.string.login_api_name_my_na_ovoce;
    }

    @Override
    public boolean isCustomNaOvoceAPI() {
        return true;
    }

    @Override
    public int radioButtonId() {
        return 0;
    }

    @Override
    public boolean wantsToProvideMarkers() {
        return true;
    }

    @Override
    protected API tryLoadFromId(String id) {
        String[] data = id.split(ID_SEPERATOR);
        if (data.length == 0) {
            return null;
        }
        String classId = data[0];
        if (!classId.equals(Settings.API_ID_MY_NA_OVOCE)) {
            return null;
        }
        if (data.length > 1) {
            String host = data[1];
            return getInstance(host);
        }
        return CustomNaOvoceLoginAPI.instance();
    }

    public static API getInstance(String host) {
        if (apis.containsKey(host)) {
            return apis.get(host);
        }
        CustomNaOvoceAPI api = new CustomNaOvoceAPI(host);
        apis.put(host, api);
        return api;
    }
}
