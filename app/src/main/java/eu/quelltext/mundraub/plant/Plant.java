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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import eu.quelltext.mundraub.Helper;
import eu.quelltext.mundraub.map.MapCache;


public class Plant implements Comparable<Plant> {

    private static final PlantCollection plants = new PersistentPlantCollection();
    private static final MapCache mapCache = new MapCache();

    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_COUNT = "count";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_ID = "id";
    private static final String JSON_CATEGORY = "category";
    private static final String JSON_PICTURE = "picture";
    private static final String JSON_ONLINE = "online";

    public static List<Plant> all() {
        return plants.all();
    }

    public static Plant withId(String id) {
        return plants.withId(id);
    }

    public static boolean withIdExists(String id) {
        return plants.contains(id);
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
        this.id = plants.newId();
        this.collection = plants;
        this.onlineState = PlantOnlineState.getOfflineState(this);
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
        mapCache.removeMapPreviewOf(this);
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
        json.put(JSON_LONGITUDE, position.getLongitude());
        json.put(JSON_LATITUDE, position.getLatitude());
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

    public Plant(PlantCollection collection, JSONObject json) throws JSONException {
        this.collection = collection;
        id = json.getString(JSON_ID);
        setPosition(new Position(json.getDouble(JSON_LONGITUDE), json.getDouble(JSON_LATITUDE)));
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
        return picture != null && picture.isFile();
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
        mapCache.initilizeOnCacheDirectoryFrom(imageView.getContext());
        final Plant plant = this;
        mapCache.mapPreviewOf(this, new MapCache.Callback() {
            @Override
            public void onSuccess(File file) {
                if (setBitmapFromFileOrNull(file, imageView)) {
                    callback.onSuccess(file);
                } else {
                    Log.d("setPictureToMap", "Retry with new picture of " + plant.getId());
                    mapCache.removeMapPreviewOf(plant);
                    mapCache.mapPreviewOf(plant, new MapCache.Callback() {
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
        return plants.getCreationDate(this);
    }

    @Override
    public int compareTo(@NonNull Plant other) {
        return -createdAt().compareTo(other.createdAt());
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
        if (mapCache != null) {
            mapCache.mapPreviewOf(this);
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

    static public class Position {
        public static final Position NULL = new Position(0, 0);
        private static final double MAP_IMAGE_BOUNDARY = 0.002;
        private static final int MAP_IMAGE_SCALE = 200000;

        private final double longitude;
        private final double latitude;

        protected static Position from(Location location) {
            return new Position(location.getLongitude(), location.getLatitude());
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
    }
}
