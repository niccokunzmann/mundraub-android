package eu.quelltext.mundraub.map;

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

    protected byte[] getResponseBytesFromPlantMarkerQuery(String queryParameterString) throws IOException {
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


/*    private final HttpServer server;
    private final static int DEFAULT_PORT = 39768;
    private static MundraubProxy instance = null;

    private MundraubMapAPI() throws IOException {
        this(DEFAULT_PORT);
    }

    private MundraubMapAPI(int port) throws IOException {
        // see https://www.codeproject.com/tips/1040097/create-a-simple-web-server-in-java-http-server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/plant", new PlantHandler()); // https://mundraub.org/cluster/plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37
        //server.setExecutor(null);
        server.setExecutor(Executors.newFixedThreadPool(5)); // see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html
    }



    public void run() {
        server.start();
    }


    private OkHttpClient client() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        return builder
                .followRedirects(false) // from https://stackoverflow.com/a/29268150/1320237
                .followSslRedirects(false)
                .build();
    }

    protected class PlantHandler implements HttpHandler {

        String API_PROTOCOL = "https";
        String API_HOST = "mundraub.org";
        String API_PATH = "/cluster/plant";

        @Override
        public void handle(HttpExchange exchange) {
            try {
                HttpUrl url = buildUrl(exchange);
                Response response = httpGet(url);
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*"); // allow JavaScript to access the content
                headers.add("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = response.body().bytes();
                exchange.sendResponseHeaders(response.code(), bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch(Exception e) {
                handleError(e);
            }
        }

        private HttpUrl buildUrl(HttpExchange exchange) throws MalformedURLException {
            URI uri = exchange.getRequestURI();
            HttpUrl result = HttpUrl.parse(API_PROTOCOL + "://" + API_HOST + API_PATH + "?" + uri.getQuery());
            return result;
        }
    }

    private void handleError(Exception e) {
        e.printStackTrace();
    }

    private Response httpGet(HttpUrl url) throws IOException {
        System.out.println("GET: " + url.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .method("GET", null)
                .build();
        Response response = client().newCall(request).execute();
        return response;
    }*/
}
