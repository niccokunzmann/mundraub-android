package eu.quelltext.mundraub.plant;

import java.util.List;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Plant {

    private static final PlantCollection plants = new PlantCollection();

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
    }

    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public void setCount(int count) {
        this.count = count;
        save();
    }
}
