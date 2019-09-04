package eu.quelltext.mundraub.map.handler;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.UnsupportedEncodingException;

import eu.quelltext.mundraub.map.MundraubMapAPI;

public class MockPlantCollectionHandler extends URIHandler {

    private static String plantsJSON = "" +
            "[" +
            "   {\n" +
            "     \"picture\": \"\\/mnt\\/sdcard\\/eu.quelltext.mundraub\\/2019-08-01-09-55-05\\/plant.jpg\",\n" +
            "     \"id\": \"2019-08-01-09-55-05\",\n" +
            "     \"position\": {\n" +
            "       \"type\": \"map\",\n" +
            "       \"longitude\": 13.097033948961839,\n" +
            "       \"latitude\": 52.38587459217477\n" +
            "     },\n" +
            "     \"category\": \"walnut\",\n" +
            "     \"count\": 1,\n" +
            "     \"description\": \"Vor dem Eingang des Elefantenhauses.\",\n" +
            "     \"online\": {\n" +
            "       \"id\": \"83381\",\n" +
            "       \"type\": \"online\",\n" +
            "       \"api\": \"mundraub\"\n" +
            "     }\n" +
            "   }," +
            "   {\n" +
            "     \"picture\": \"\\/mnt\\/sdcard\\/eu.quelltext.mundraub\\/2019-09-03-16-37-40\\/plant.jpg\",\n" +
            "     \"id\": \"2019-09-03-16-37-40\",\n" +
            "     \"position\": {\n" +
            "       \"type\": \"map\",\n" +
            "       \"longitude\": 13.099319191043191,\n" +
            "       \"latitude\": 52.38224016778912\n" +
            "     },\n" +
            "     \"category\": \"walnut\",\n" +
            "     \"count\": 1,\n" +
            "     \"description\": \"Hinter der Mirabelle.\",\n" +
            "     \"online\": {\n" +
            "       \"type\": \"offline\"\n" +
            "     }\n" +
            "   }" +
            "]";

    public MockPlantCollectionHandler(String uri, MundraubMapAPI errorHandler) {
        super(uri, errorHandler);
    }

    @Override
    public Response respondTo(IHTTPSession input) throws UnsupportedEncodingException {
        byte[] bytes = plantsJSON.getBytes("UTF-8");
        Response result = Response.newFixedLengthResponse(Status.OK, "application/json", bytes);
        return result;
    }

}
