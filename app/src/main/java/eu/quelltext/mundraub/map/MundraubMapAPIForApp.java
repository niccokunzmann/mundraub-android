package eu.quelltext.mundraub.map;

import android.app.Activity;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.plant.PlantCategory;
import okhttp3.OkHttpClient;

public class MundraubMapAPIForApp extends MundraubMapAPI {

    private final Logger.Log log;

    public MundraubMapAPIForApp() {
        super(Settings.hostForMundraubAPI());
        log = Logger.newFor(this);
        addHTTPInterceptor(new TilesHandler(MapUrl.PATH_SATELITE, TilesCache.forSatellite()));
        addHTTPInterceptor(new TilesHandler(MapUrl.PATH_OSM, TilesCache.forOSM()));
    }

    @Override
    protected void handleError(Exception e) {
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

    protected class TilesHandler extends URIHandler {
        private final TilesCache cache;

        public TilesHandler(String uri, TilesCache cache) {
            super(uri);
            this.cache = cache;
        }

        @Override
        public boolean wantsToServeURI(String uri) {
            return uri.startsWith(baseUri());
        }

        public Response respondTo(IHTTPSession input) throws Exception {
            String[] zyx = input.getUri().substring(this.baseUri().length()).split("/");
            if (zyx.length < 3) {
                return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, "I need " + baseUri() + "z/y/x");
            }
            int x = Integer.parseInt(zyx[zyx.length - 1]);
            int y = Integer.parseInt(zyx[zyx.length - 2]);
            int z = Integer.parseInt(zyx[zyx.length - 3]);
            TilesCache.Tile tile = cache.getTileAt(x, y, z);
            if (tile.isCached()) {
                log.d("handle tile", 200 + ": " + z + "/" + y + "/" + x);
                Response response = Response.newFixedLengthResponse(Status.OK, tile.contentType(), tile.bytes());
                response.addHeader("Cache-Control", "no-store, must-revalidate"); // no cache from https://stackoverflow.com/a/2068407
                return response;
            } else {
                log.d("handle tile", 307 + ": " + z + "/" + y + "/" + x);
                Response response = Response.newFixedLengthResponse(Status.TEMPORARY_REDIRECT, MIME_PLAINTEXT, "Image not found.");
                response.addHeader("Location", tile.url());
                return response;
            }
        }
    }
}
