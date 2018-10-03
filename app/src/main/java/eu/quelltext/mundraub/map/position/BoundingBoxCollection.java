package eu.quelltext.mundraub.map.position;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class BoundingBoxCollection {

    private Set<BoundingBox> boxes = new HashSet<>();

    public static BoundingBoxCollection empty() {
        return new BoundingBoxCollection();
    }

    public static BoundingBoxCollection fromJSONString(String jsonString) {
        BoundingBoxCollection bboxes = empty();
        try {
            JSONArray bboxesJSON = new JSONArray(jsonString);
            for (int i = 0; i < bboxesJSON.length(); i++) {
                bboxes.add(BoundingBox.fromJSON(bboxesJSON.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace(); // will never happen I suppose
        }
        return bboxes;
    }

    public BoundingBoxCollection add(BoundingBox boundingBox) {
        boxes.add(boundingBox);
        return this;
    }

    public String toJSONString() {
        JSONArray bboxesJSON = new JSONArray();
        for (BoundingBox bbox : boxes) {
            bboxesJSON.put(bbox.toJSON());
        }
        return bboxesJSON.toString();
    }

    public static BoundingBoxCollection with(BoundingBox boundingBox) {
        return empty().add(boundingBox);
    }

    public static BoundingBoxCollection with(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        return empty().add(boundingBox1).add(boundingBox2);
    }

    public int size() {
        return boxes.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return super.equals(obj);
        }
        BoundingBoxCollection other = (BoundingBoxCollection) obj;
        return other.asSet().equals(asSet());
    }

    private Set<BoundingBox> asSet() {
        return boxes;
    }

    @Override
    public int hashCode() {
        return asSet().hashCode();
    }
}
