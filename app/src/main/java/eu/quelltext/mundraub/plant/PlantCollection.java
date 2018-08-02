package eu.quelltext.mundraub.plant;

import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantCollection {

    private Map<String, Plant> idToPlant;

    public PlantCollection() {
        idToPlant = new HashMap<String, Plant>();
    }

    public List<Plant> all() {
        return new ArrayList<Plant>(idToPlant.values());
    }

    public String newId() {
        // from https://stackoverflow.com/a/6953926
        return DateFormat.format("yyyy-MM-dd_HH.mm.ss", Calendar.getInstance().getTime()).toString();
    }

    public Plant withId(String id) {
        return idToPlant.get(id);
    }

    public void save(Plant plant) {
        idToPlant.put(plant.getId(), plant);
    }

    public void delete(Plant plant) {
        idToPlant.remove(plant.getId());
    }
}
