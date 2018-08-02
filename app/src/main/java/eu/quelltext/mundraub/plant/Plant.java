package eu.quelltext.mundraub.plant;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Plant {

    private static final PlantCollection plants = new PersistentPlantCollection();
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_COUNT = "count";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_ID = "id";
    private static final String JSON_CATEGORY = "category";

    public static List<Plant> all() {
        return plants.all();
    }

    public static Plant withId(String id) {
        return plants.withId(id);
    }

    // instance

    private final String id;
    private PlantCategory category;
    private String description;
    private int count;
    private double longitude;
    private double latitude;
    private final PlantCollection collection;

    public Plant() {
        this.id = plants.newId();
        this.collection = plants;
        save();
    }

    public void save() {
        this.collection.save(this);
    }

    public String getId() {
        return id;
    }


    public String getContent() {
        return "Test Content for Plant " + getId();
    }

    public String getDetails() {
        return "Test Details String for Plant " + getId();
    }

    public void delete() {
        collection.delete(this);
    }

    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public void setCount(int count) {
        this.count = count;
        save();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_LONGITUDE, longitude);
        json.put(JSON_LATITUDE, latitude);
        json.put(JSON_COUNT, count);
        json.put(JSON_DESCRIPTION, description);
        json.put(JSON_ID, id);
        if (hasCategory()) {
            json.put(JSON_CATEGORY, category.getId());
        } else {
            json.put(JSON_CATEGORY, null);
        }
        return json;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public int getCount() {
        return count;
    }

    public String getDescription() {
        return description;
    }

    public void setLocation(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        save();
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasPosition() {
        return longitude != 0 && latitude != 0;
    }

    public void setCategory(PlantCategory category) {
        this.category = category;
        save();
    }
}
