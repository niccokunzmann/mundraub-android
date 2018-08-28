package eu.quelltext.mundraub.plant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.map.MapCache;


public class Plant extends ErrorAware implements Comparable<Plant> {

    private static PlantCollection plants = null;
    private static MapCache mapCache = null;

    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_COUNT = "count";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_ID = "id";
    private static final String JSON_CATEGORY = "category";
    private static final String JSON_PICTURE = "picture";
    private static final String JSON_ONLINE = "online";
    private static final String JSON_POSITION = "position";
    private Date creationDate;
    protected long creationDateMillis;

    public static List<Plant> all() {
        return getPlants().all();
    }

    public static Plant withId(String id) {
        return getPlants().withId(id);
    }

    public static boolean withIdExists(String id) {
        return getPlants().contains(id);
    }

    // instance

    private final String id;
    private PlantCategory category = PlantCategory.NULL;
    private String description = "";
    private int count = 0;
    private final PlantCollection collection;
    private File picture = null;
    private PlantOnlineState.OnlineAction onlineState;
    private Position position = Position.NULL;

    public Plant() {
        super();
        this.id = getPlants().newId();
        this.collection = getPlants();
        this.onlineState = PlantOnlineState.getOfflineState(this);
    }

    public Plant(PlantCollection collection, JSONObject json) throws JSONException {
        super();
        this.collection = collection;
        id = json.getString(JSON_ID);
        if (json.has(JSON_LONGITUDE) && json.has(JSON_LATITUDE)) {
            // irrelevant after 14th of August 2018
            setPosition(new Position(json.getDouble(JSON_LONGITUDE), json.getDouble(JSON_LATITUDE)));
        } else {
            setPosition(Position.from(json.getJSONObject(JSON_POSITION)));
        }
        count = json.getInt(JSON_COUNT);
        description = json.getString(JSON_DESCRIPTION);
        if (json.has(JSON_CATEGORY)) {
            category = PlantCategory.withId(json.getString(JSON_CATEGORY));
        } else {
            category = PlantCategory.NULL;
        }
        if (json.has(JSON_PICTURE)) {
            String picturePath = json.getString(JSON_PICTURE);
            if (picturePath != null) {
                picture = new File(picturePath);
            }
        }
        if (json.has(JSON_ONLINE) && !json.isNull(JSON_ONLINE)) {
            onlineState = PlantOnlineState.fromJSON(this, json.getJSONObject(JSON_ONLINE));
        } else {
            onlineState = PlantOnlineState.getOfflineState(this);
        }
    }

    private static PlantCollection getPlants() {
        if (plants == null) {
            plants = new PersistentPlantCollection();
        }
        return plants;
    }

    private static MapCache getMapCache() {
        if (mapCache == null) {
            mapCache = new MapCache();
        }
        return mapCache;
    }

    private void ensureCreationDate() {
        if (creationDate == null) {
            creationDate = getPlants().getCreationDate(this);
            creationDateMillis = creationDate.getTime();
        }
    }

    public void save() {
        this.collection.save(this);
        Log.d("PLANT", "saved " + getId());
    }

    public String getId() {
        return id;
    }


    public String getDetailsTitle() {
        return getId();
    }

    public void delete() {
        collection.delete(this);
        getMapCache().removeMapPreviewOf(this);
    }

    public void setDescription(String description) {
        this.description = description;
        save();
    }

    public void setCount(int count) {
        if (count != this.count) {
            this.count = count;
            save();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_POSITION, position.toJSON());
        json.put(JSON_COUNT, count);
        json.put(JSON_DESCRIPTION, description);
        json.put(JSON_ID, id);
        if (hasCategory()) {
            json.put(JSON_CATEGORY, category.getId());
        } else {
            json.put(JSON_CATEGORY, null);
        }
        if (hasPicture()) {
            json.put(JSON_PICTURE, picture.toString());
        }else {
            json.put(JSON_PICTURE, null);
        }
        json.put(JSON_ONLINE, onlineState.toJSON());
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
        setPosition(Position.from(location));
        save();
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasPosition() {
        return position.isValid();
    }

    public void setCategory(PlantCategory category) {
        this.category = category;
        save();
    }

    public void setPicture(File picture) {
        this.picture = picture;
        save();
    }

    public boolean hasPicture() {
        return picture != null;
    }

    public File getPicture() {
        return picture;
    }

    private boolean setBitmapFromFileOrNull(File file, ImageView imageView) {
        if (file == null) return false;
        Uri uri = Uri.fromFile(file);
        Context context = imageView.getContext();
        Bitmap bitmap;
        try {
            // from https://stackoverflow.com/a/31930502/1320237
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            return false;
        }
        if (bitmap == null) {
            return false;
        }
        imageView.setImageBitmap(bitmap);
        return true;
    }

    public void setPictureToPlant(ImageView imageView) {
        Context context = imageView.getContext();
        if (setBitmapFromFileOrNull(getPicture(), imageView)) {
            return;
        }
        // from https://stackoverflow.com/a/11737758/1320237
        String uri = "@android:drawable/ic_menu_gallery";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable resource = context.getResources().getDrawable(imageResource);
        imageView.setImageDrawable(resource);
    }

    public void setPictureToMap(final ImageView imageView, final MapCache.Callback callback) {
        final Plant plant = this;
        getMapCache().mapPreviewOf(this, new MapCache.Callback() {
            @Override
            public void onSuccess(File file) {
                if (setBitmapFromFileOrNull(file, imageView)) {
                    callback.onSuccess(file);
                } else {
                    Log.d("setPictureToMap", "Retry with new picture of " + plant.getId());
                    getMapCache().removeMapPreviewOf(plant);
                    getMapCache().mapPreviewOf(plant, new MapCache.Callback() {
                        @Override
                        public void onSuccess(File file) {
                            if (setBitmapFromFileOrNull(file, imageView)) {
                                callback.onSuccess(file);
                            } else {
                                callback.onFailure();
                                Log.d("setPictureToMap", "Could not set picture " + file.toString());
                            }
                        }

                        @Override
                        public void onFailure() {
                            callback.onFailure();
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                callback.onFailure();
            }
        });

    }

    public boolean movePictureTo(File newLocation) {
        if (picture.renameTo(newLocation) || (!picture.isFile() && newLocation.isFile())) {
            Log.d("PICTURE", "Moved picture from " + picture.toString() + " to " + newLocation.toString());
            picture = newLocation;
            save();
            return true;
        }
        return false;
    }

    private Date createdAt() {
        ensureCreationDate();
        return creationDate;
    }

    @Override
    public int compareTo(@NonNull Plant other) {
        return Helper.compare(other.createdAtLong(), createdAtLong());
    }

    private long createdAtLong() {
        ensureCreationDate();
        return creationDateMillis;
    }

    public Date getCreationDay() {
        return DateUtils.truncate(createdAt(), Calendar.DAY_OF_MONTH);
    }

    public boolean hasRequiredFieldsFilled() {
        return count >= 0 && !category.isUnknown() && !description.isEmpty();
    }

    public PlantOnlineState.OnlineAction online() {
        return onlineState;
    }

    public void setOnline(PlantOnlineState.OnlineAction online) {
        onlineState = online;
        save();
    }

    public String getFormCount() {
        // from https://mundraub.org/node/add/plant/
        if (count == 0) {
            return "_none";
        } else if (count == 1) {
            return "0";
        } else if (2 <= count && count <= 5) {
            return "1";
        } else if (6 <= count && count <= 10) {
            return "2";
        } else if (10 < count) {
            return "3";
        }
        // negative number
        return "_none";
    }

    public String getPathComponent() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    private void setPosition(Position position) {
        this.position = position;
        if (getMapCache() != null) {
            getMapCache().mapPreviewOf(this);
        }
    }

    public double getLongitude() {
        return getPosition().getLongitude();
    }
    public double getLatitude() {
        return getPosition().getLatitude();
    }

    public boolean exists() {
        return collection.contains(getId());
    }

    public void setPositionFromMapUrl(String url) {
        try {
            setPosition(Position.fromMapWithMarker(url));
        } catch (MalformedURLException e) {
            log.printStackTrace(e);
        }
    }

    public boolean shouldAskTheUserAboutPlacementBeforeUpload() {
        return getPosition().shouldAskTheUserAboutPlacementBeforeUpload();
    }

    public int getRepositionReason() {
        return getPosition().getRepositionReason();
    }

    public Position getBestPositionForMap() {
        Position position = getPosition();
        if (position.isValid()) {
            return position;
        }
        return getPositionOfClosestPlantByTime();
    }

    private Position getPositionOfClosestPlantByTime() {
        return getClosestPlantByTimeWithPosition().getPosition();
    }

    private Plant getClosestPlantByTimeWithPosition() {
        List<Plant> allPlantsByTimeDifference = all();
        final long reference = this.createdAtLong();
        Collections.sort(allPlantsByTimeDifference, new Comparator<Plant>() {
            @Override
            public int compare(Plant plant1, Plant plant2) {
                return Helper.compare(
                        Math.abs(plant1.createdAtLong() - reference),
                        Math.abs(plant2.createdAtLong() - reference));
            }
        });
        for (Plant plant : allPlantsByTimeDifference) {
            if (plant.getPosition().isValid()) {
                return plant;
            }
        }
        return this;
    }

    static public class Position {
        public static final Position NULL = new Position(0, 0);
        protected static final String JSON_POSITION_TYPE = "type";
        protected static final String JSON_POSITION_TYPE_GPS = "gps";
        protected static final String JSON_POSITION_TYPE_UNKNOWN = "unknown";
        protected static final String JSON_POSITION_TYPE_MAP = "map";

        private static final double MAP_IMAGE_BOUNDARY = 0.002;
        private static final int MAP_IMAGE_SCALE = 200000;

        private final double longitude;
        private final double latitude;

        protected static Position from(Location location) {
            return new GPSPosition(location);
        }

        private Position(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public String asId() {
            return Helper.doubleTo15DigitString(getLongitude()) + "-" + Helper.doubleTo15DigitString(getLatitude());
        }

        public String forAPI() {
            // POINT(6.968046426773072 50.82075362541587)
            return "POINT(" +
                    Helper.doubleTo15DigitString(getLongitude()) + " " +
                    Helper.doubleTo15DigitString(getLatitude()) + ")";
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public boolean isValid() {
            return longitude != 0 && latitude != 0;
        }

        public String getOpenStreetMapExportUrl(String format) {
            // http://staticmap.openstreetmap.de/staticmap.php?markers=47.303785900000000,11.521939200000000,lightblue1&center=47.303785900000000,11.521939200000000&zoom=18&size=200x200&maptype=mapnik
            String lon = Helper.doubleTo15DigitString(getLongitude());
            String lat = Helper.doubleTo15DigitString(getLatitude());
            return "http://staticmap.openstreetmap.de/staticmap.php?markers=" + lat + "," + lon + ",lightblue1&center=" + lat + "," + lon + "&zoom=18&size=200x200&maptype=outdoors";
        }

        public String getOpenStreetMapAddressUrl() {
            // examples:
            // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469471
            // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469470
            // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469469
            // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469468
            return "https://nominatim.openstreetmap.org/reverse?zoom=18&lon=" +
                    getLongitude() + "&lat=" + getLatitude() + "&format=json";
        }

        public boolean equals(Position other) {
            return getLongitude() == other.getLongitude() && getLatitude() == other.getLatitude();
        }

        public String asCallbackId() {
            return asId();
        }

        private static Position fromMapWithMarker(String url_) throws MalformedURLException {
            Log.d("POSITION FROM URL", url_);
            URL url = new URL(url_); // examples/fullScreen.html#11.523992844180245,47.30569859911609
            String ref = url.getRef(); // 11.523992844180245,47.30569859911609
            String[] parts = ref.split(","); // from http://stackoverflow.com/questions/3481828/ddg#3481842
            String longitude = parts[0]; // 11.523992844180245
            String latitude = parts[1];  // 47.30569859911609
            return new MapPosition(Double.parseDouble(longitude), Double.parseDouble(latitude));
        }

        public String getMapURLWithMarker() {
            // from https://stackoverflow.com/a/5749641/1320237
            return "file:///android_asset/map/examples/fullScreen.html?" + getLongitude() + "," + getLatitude();
        }

        public static Position from(JSONObject json) throws JSONException {
            String type = json.getString(JSON_POSITION_TYPE);
            if (type == JSON_POSITION_TYPE_MAP) {
                return MapPosition.fromJSON(json);
            } else if (type == JSON_POSITION_TYPE_GPS) {
                return GPSPosition.fromJSON(json);
            }
            return fromJSON(json);
        }

        protected static Position fromJSON(JSONObject json) throws JSONException {
            return new Position(json.getDouble(JSON_LONGITUDE), json.getDouble(JSON_LATITUDE));
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(JSON_POSITION_TYPE, JSON_POSITION_TYPE_UNKNOWN);
            json.put(JSON_LONGITUDE, getLongitude());
            json.put(JSON_LATITUDE, getLatitude());
            return json;
        }

        public boolean shouldAskTheUserAboutPlacementBeforeUpload() {
            return true;
        }

        public int getRepositionReason() {
            return R.string.reason_reposition_unknown_position;
        }
    }

    static private class MapPosition extends Position {

        private MapPosition(double longitude, double latitude) {
            super(longitude, latitude);
        }
        public JSONObject toJSON() throws JSONException {
            JSONObject json = super.toJSON();
            json.put(JSON_POSITION_TYPE, JSON_POSITION_TYPE_MAP);
            return json;
        }

        protected static Position fromJSON(JSONObject json) throws JSONException {
            return new MapPosition(json.getDouble(JSON_LONGITUDE), json.getDouble(JSON_LATITUDE));
        }

        public boolean shouldAskTheUserAboutPlacementBeforeUpload() {
            return false;
        }

        public int getRepositionReason() {
            return R.string.error_not_implemented;
        }
    }

    static private class GPSPosition extends Position {

        private static final String JSON_ACCURACY = "accuracy";
        private final double accuracy; // within X meters at 68% probability, see https://stackoverflow.com/a/13807786

        private GPSPosition(Location location) {
            super(location.getLongitude(), location.getLatitude());
            this.accuracy = location.getAccuracy();
            // this.timeToRecord = location.getTime(); // TODO: add time
        }

        public GPSPosition(JSONObject json) throws JSONException {
            super(json.getDouble(JSON_LONGITUDE), json.getDouble(JSON_LATITUDE));
            accuracy = json.getDouble(JSON_ACCURACY);
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = super.toJSON();
            json.put(JSON_POSITION_TYPE, JSON_POSITION_TYPE_GPS);
            json.put(JSON_ACCURACY, accuracy);
            return json;
        }

        protected static Position fromJSON(JSONObject json) throws JSONException {
            return new GPSPosition(json);
        }

        public int getRepositionReason() {
            return R.string.reason_reposition_gps_position;
        }
    }
}
