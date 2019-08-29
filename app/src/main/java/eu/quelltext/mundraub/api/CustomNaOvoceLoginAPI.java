package eu.quelltext.mundraub.api;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Settings;

public class CustomNaOvoceLoginAPI extends CustomNaOvoceAPI {

    private static CustomNaOvoceAPI instance = null;

    public CustomNaOvoceLoginAPI(String host) {
        super(host);
    }

    @Override
    public boolean wantsToProvideMarkers() {
        return false;
    }

    public static API instance() {
        String host = Settings.getCustomNaOvoceHost();
        if (instance == null || !instance.host().equals(host)) {
            instance = new CustomNaOvoceLoginAPI(host);
        }
        return instance;
    }

    @Override
    public int radioButtonId() {
        return R.id.radioButton_my_na_ovoce;
    }

    @Override
    public String id() {
        return Settings.API_ID_MY_NA_OVOCE;
    }
}
