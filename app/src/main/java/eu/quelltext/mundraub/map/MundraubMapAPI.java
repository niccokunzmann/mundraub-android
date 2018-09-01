package eu.quelltext.mundraub.map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
    This is a proxy to the Mundraub API for the map.
    It can be extended to cache plants.
 */
public class MundraubMapAPI implements Runnable {

    private final HttpServer server;
    private final static int DEFAULT_PORT = 39768;

    public MundraubMapAPI() throws IOException {
        this(DEFAULT_PORT);
    }

    public MundraubMapAPI(int port) throws IOException {
        // see https://www.codeproject.com/tips/1040097/create-a-simple-web-server-in-java-http-server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/plant", new PlantHandler()); // https://mundraub.org/cluster/plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37
        //server.setExecutor(null);
        server.setExecutor(Executors.newFixedThreadPool(5)); // see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public static void main(String[] args){
        try {
            int port = Integer.parseInt(args[0]);
            MundraubMapAPI api = new MundraubMapAPI(port);
            System.out.println("MundraubMapAPI started at http://localhost:" + api.getPort() + "/");
            api.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }
}
