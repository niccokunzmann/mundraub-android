package eu.quelltext.mundraub.map;

import org.json.JSONException;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.IHandler;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;


/*
    This is a proxy to the Mundraub API for the map.
    It can be extended to cache plants.
 */
public class MundraubMapAPI extends NanoHTTPD implements MundraubProxy {

    private final static int DEFAULT_PORT = 39768;

    @Override
    public void start() throws IOException {
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public void stop() {
        super.stop();
    }

    protected MundraubMapAPI() {
        super("localhost", DEFAULT_PORT);
        addHTTPInterceptor(new PlantHandler());
    }


    public int getPort() {
        return getListeningPort() > 0 ? getListeningPort() : DEFAULT_PORT;
    }


    public String getUrl() {
        return "http://" + getHostname() + ":" + getPort() + "/";
    }

    public static void main(String[] args){
        try {
            MundraubMapAPI api = new MundraubMapAPI();
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

    class PlantHandler implements IHandler<IHTTPSession, Response>  {

        @Override
        public Response handle(IHTTPSession input) {
            System.out.println("input.getMethod(): " + input.getMethod() + " " + (input.getMethod() == Method.GET));
            System.out.println("input.getUri(): " + input.getUri() + " " + input.getUri().equals("/plant"));
            if (input.getMethod() != Method.GET || !input.getUri().equals("/plant")) {
                return null;
            }
            try {
                byte[] bytes = getResponseBytesFromPlantMarkerQuery(input.getQueryParameterString());
                Response result = Response.newFixedLengthResponse(Status.OK, "application/json", bytes);
                result.addHeader("Access-Control-Allow-Origin", "*"); // allow JavaScript to access the content
                result.addHeader("Content-Type", "application/json; charset=UTF-8");
                return result;
            } catch(Exception e) {
                handleError(e);
                return null;
            }
        }
    }

    String API_PROTOCOL = "https";
    String API_HOST = "mundraub.org";
    String API_PATH = "/cluster/plant";

    protected byte[] getResponseBytesFromPlantMarkerQuery(String queryParameterString) throws IOException, JSONException {
        HttpUrl url = HttpUrl.parse(API_PROTOCOL + "://" + API_HOST + API_PATH + "?" + queryParameterString);
        okhttp3.Response response = httpGet(url);
        return response.body().bytes();
    }

    protected void handleError(Exception e) {
        e.printStackTrace();
    }

    protected okhttp3.Response httpGet(HttpUrl url) throws IOException {
        System.out.println("GET: " + url.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .method("GET", null)
                .build();
        okhttp3.Response response = client().newCall(request).execute();
        return response;
    }

    protected OkHttpClient client() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder
                .followRedirects(false) // from https://stackoverflow.com/a/29268150/1320237
                .followSslRedirects(false)
                .build();
    }
}
