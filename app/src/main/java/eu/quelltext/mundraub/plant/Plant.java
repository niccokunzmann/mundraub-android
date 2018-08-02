package eu.quelltext.mundraub.plant;

import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Plant {

    public static List<Plant> all() {
        return new ArrayList<Plant>(idToPlant.values());
    }

    private static final Map<String, Plant> idToPlant = new HashMap<String, Plant>();


    static {
    }

    private static void addPlant(Plant plant) {
        idToPlant.put(plant.getId(), plant);
        plant.save();
    }


    private static String newId() {
        // from https://stackoverflow.com/a/6953926
        return DateFormat.format("yyyy-MM-dd_HH.mm.ss", Calendar.getInstance().getTime()).toString();
    }

    public static Plant withId(String id) {
        return idToPlant.get(id);
    }

    // instance

    private final String id;
    private PlantCategory category;
    private String description;
    private int count;
    private double longitude;
    private double latitude;


    public Plant() {
        id = newId();
        addPlant(this);
    }

    private void save() {

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
}
