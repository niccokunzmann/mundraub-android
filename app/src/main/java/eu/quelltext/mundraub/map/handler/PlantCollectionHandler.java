package eu.quelltext.mundraub.map.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.UnsupportedEncodingException;
import java.util.List;

import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCollection;

public class PlantCollectionHandler extends URIHandler {
    private final PlantCollection plants;

    public PlantCollectionHandler(String uri, PlantCollection plants, ErrorHandler errorHandler) {
        super(uri, errorHandler);
        this.plants = plants;
    }

    @Override
    public Response respondTo(IHTTPSession input) throws JSONException, UnsupportedEncodingException {
        List<Plant> plantList = plants.all();
        JSONArray json = new JSONArray();
        for (Plant plant: plantList) {
            JSONObject plantJSON = plant.toJSON();
            json.put(plantJSON);
        }
        String string = json.toString();
        byte[] bytes = string.getBytes("UTF-8");
        Response result = Response.newFixedLengthResponse(Status.OK, "application/json", bytes);
        return result;
    }
}
