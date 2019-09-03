package eu.quelltext.mundraub.map;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import eu.quelltext.mundraub.map.handler.ErrorHandler;
import eu.quelltext.mundraub.map.handler.URIHandler;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/*
    This is a proxy to the Mundraub API for the map.
    It can be extended to cache plants.
 */
public class MundraubMapAPI extends NanoHTTPD implements MundraubProxy, ErrorHandler {

    private final static int DEFAULT_PORT = 39768;
    private static final String API_PATH_TRANSLATIONS = "/translations/app.js";


    @Override
    public void start() throws IOException {
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public void stop() {
        super.stop();
    }

    protected MundraubMapAPI(String hostname) {
        super(hostname, DEFAULT_PORT);
        addHTTPInterceptor(new PlantHandler(API_PATH));
        addHTTPInterceptor(new TranslationsHandler(API_PATH_TRANSLATIONS));
    }

    public int getPort() {
        return getListeningPort() > 0 ? getListeningPort() : DEFAULT_PORT;
    }


    public String getUrl() {
        return "http://" + getHostname() + ":" + getPort() + "/";
    }

    public static void main(String[] args){
        try {
            MundraubMapAPI api = new MundraubMapAPI("0.0.0.0");
            System.out.println("MundraubMapAPI started at " + api.getUrl());
            api.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>404 Nothing to see here</h1>\n" +
                "See <a href=\"https://github.com/niccokunzmann/mundraub-android" +
                "/blob/master/docs/\">the documentation</a>.";
        return Response.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_HTML, msg);
    }

    protected class PlantHandler extends URIHandler {

        protected PlantHandler(String uri) {
            super(uri, MundraubMapAPI.this);
        }

        public Response respondTo(IHTTPSession input) throws Exception {
            byte[] bytes = getResponseBytesFromPlantMarkerQuery(input.getQueryParameterString());
            Response result = Response.newFixedLengthResponse(Status.OK, "application/json", bytes);
            result.addHeader("Content-Type", "application/json; charset=UTF-8");
            return result;
        }
    }

    String API_PROTOCOL = "https";
    String API_HOST = "mundraub.org";
    String API_PATH = "/cluster/plant";

    protected byte[] getResponseBytesFromPlantMarkerQuery(String queryParameterString) throws IOException, Exception /*JSONException*/ {
        HttpUrl url = HttpUrl.parse(API_PROTOCOL + "://" + API_HOST + API_PATH + "?" + queryParameterString);
        okhttp3.Response response = httpGet(url);
        return response.body().bytes();
    }

    public void handleError(Exception e) {
        e.printStackTrace();
    }

    protected okhttp3.Response httpGet(HttpUrl url) throws IOException {
        debug("GET: " + url.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .method("GET", null)
                .build();
        okhttp3.Response response = client().newCall(request).execute();
        return response;
    }

    protected void debug(String s) {
        System.out.println(s);
    }

    protected OkHttpClient client() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder
                .followRedirects(false) // from https://stackoverflow.com/a/29268150/1320237
                .followSslRedirects(false)
                .build();
    }

    protected class TranslationsHandler extends URIHandler {

        public TranslationsHandler(String uri) {
            super(uri, MundraubMapAPI.this);
        }

        @Override
        public Response respondTo(IHTTPSession input) throws Exception {
            byte[] bytes = getResponseBytesForAppTranslations();
            Response result = Response.newFixedLengthResponse(Status.OK, "application/json", bytes);
            result.addHeader("Access-Control-Allow-Origin", "*"); // allow JavaScript to access the content
            result.addHeader("Content-Type", "application/json; charset=UTF-8");
            return result;
        }
    }

    protected byte[] getResponseBytesForAppTranslations() throws UnsupportedEncodingException {
        // to be overwritten by subclasses
        return "{\"apple\":\"Apfel\"}".getBytes("UTF-8");
    }
}
