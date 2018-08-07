package eu.quelltext.mundraub.plant;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantCollection {

    private Map<String, Plant> idToPlant;
    private final String ID_FORMAT = "yyyy-MM-dd-kk-mm-ss";

    public PlantCollection() {
        idToPlant = new HashMap<String, Plant>();
    }

    public List<Plant> all() {
        return new ArrayList<Plant>(idToPlant.values());
    }

    public String newId() {
        // from https://stackoverflow.com/a/6953926
        // from https://stackoverflow.com/a/24423756/1320237
        return DateFormat.format(ID_FORMAT, Calendar.getInstance().getTime()).toString();
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

    public boolean contains(String id) {
        return idToPlant.containsKey(id);
    }

    public Date getCreationDate(Plant plant) {
        SimpleDateFormat parser = new SimpleDateFormat(ID_FORMAT);
        try {
            return parser.parse(plant.getId());
        } catch (ParseException e) {
            e.printStackTrace();
            return Calendar.getInstance().getTime();
        }
    }
}
