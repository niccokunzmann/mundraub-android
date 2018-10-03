package eu.quelltext.mundraub.map.position;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.quelltext.mundraub.map.TilesCache;

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

    public long estimateTileBytesIn(List<TilesCache> caches, int[] zooms) {
        long sum = 0;
        for (TilesCache cache: caches) {
            for (int zoom : zooms) {
                sum += estimateTileBytesIn(cache, zoom);
            }
        }
        return sum;
    }

    public long estimateTileBytesIn(TilesCache cache, int zoom) {
        long sum = 0;
        for(BoundingBox bbox: boxes) {
            sum += cache.estimateTileBytesIn(bbox, zoom);
        }
        return sum;
    }

    public static String byteCountToHumanReadableString(long bytes) {
        String[] units = new String[]{"b", "kb", "mb", "gb", "tb", "eb"};
        int i = 0;
        while (bytes > 1000 && i < units.length) {
            if (bytes % 1000 > 500) {
                bytes += 1000;
            }
            bytes /= 1000;
            i++;
        }
        return bytes + units[i];
    }
}
