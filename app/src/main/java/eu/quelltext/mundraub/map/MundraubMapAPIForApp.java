package eu.quelltext.mundraub.map;

import android.app.Activity;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.map.handler.PlantCollectionHandler;
import eu.quelltext.mundraub.map.handler.TilesHandler;
import eu.quelltext.mundraub.map.handler.URIHandler;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;
import okhttp3.OkHttpClient;

public class MundraubMapAPIForApp extends MundraubMapAPI {

    private final Logger.Log log;

    public MundraubMapAPIForApp() {
        super(Settings.hostForMundraubAPI());
        log = Logger.newFor(this);
        addHTTPInterceptor(new TilesHandler(this, MapUrl.PATH_SATELITE, TilesCache.forSatellite(), this, this.log));
        addHTTPInterceptor(new TilesHandler(this, MapUrl.PATH_OSM, TilesCache.forOSM(), this, this.log));
    }

    @Override
    protected URIHandler createPlantCollectionHandler() {
        return new PlantCollectionHandler(PATH_PLANTS, Plant.getPlants(), this);
    }

    @Override
    public void handleError(Exception e) {
        log.printStackTrace(e);
    }

    public void start() throws IOException {
        super.start();
        log.d("START", this.getClass().getSimpleName() + " at " +  getUrl());
        //log.d("TEST", httpGet(HttpUrl.parse(getUrl())).body().string());
    }

    public void stop() {
        super.stop();
        log.d("STOP", this.getClass().getSimpleName() + " at " + getUrl());
    }

    @Override
    protected void debug(String s) {
        log.d("debug", s);
    }

    protected OkHttpClient client() {
        log.d("client", "for api");
        return Settings.getOkHttpClient();
    }

    @Override
    protected byte[] getResponseBytesForAppTranslations() throws UnsupportedEncodingException {
        Activity activity = Initialization.getActivity();
        Resources resources = activity.getResources();
        JSONObject json = new JSONObject();
        for (PlantCategory category : PlantCategory.all()) {
            try {
                json.put(category.getId(), resources.getString(category.getResourceId()));
            } catch (JSONException e) {
                log.printStackTrace(e);
            }
        }
        return json.toString().getBytes("UTF-8");
    }

}
