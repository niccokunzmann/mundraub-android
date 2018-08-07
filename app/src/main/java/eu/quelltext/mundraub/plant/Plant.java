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
import java.util.Date;
import java.util.List;
import java.util.Calendar;



public class Plant implements Comparable<Plant> {

    private static final PlantCollection plants = new PersistentPlantCollection();
    private static final String JSON_LONGITUDE = "longitude";
    private static final String JSON_LATITUDE = "latitude";
    private static final String JSON_COUNT = "count";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_ID = "id";
    private static final String JSON_CATEGORY = "category";
    private static final String JSON_PICTURE = "picture";

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
    private int count = 1;
    private double longitude = 0;
    private double latitude = 0;
    private final PlantCollection collection;
    private File picture = null;

    public Plant() {
        this.id = plants.newId();
        this.collection = plants;
    }

    public void save() {
        this.collection.save(this);
    }

    public String getId() {
        return id;
    }


    public String getDetailsTitle() {
        return getId();
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
        if (hasPicture()) {
            json.put(JSON_PICTURE, picture.toString());
        }else {
            json.put(JSON_PICTURE, null);
        }
        return json;
    }

    public Plant(PlantCollection collection, JSONObject json) throws JSONException {
        this.collection = collection;
        id = json.getString(JSON_ID);
        longitude = json.getDouble(JSON_LONGITUDE);
        latitude = json.getDouble(JSON_LATITUDE);
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

    private Uri getPictureUri() {
        return Uri.fromFile(getPicture()); // NullPointer? Use hasPicture() first!
    }

    private Bitmap getBitmap(Context context) throws IOException {
        // from https://stackoverflow.com/a/31930502/1320237
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), getPictureUri());
    }

    public void setImageOf(ImageView imageView) {
        Context context = imageView.getContext();
        if (hasPicture()) {
            // from https://stackoverflow.com/a/3193445/1320237
            //Bitmap bitmap = BitmapFactory.decodeFile(plant.getPicture().getAbsolutePath());
            try {
                Bitmap bitmap = getBitmap(context);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // from https://stackoverflow.com/a/11737758/1320237
        String uri = "@android:drawable/ic_menu_gallery";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable resource = context.getResources().getDrawable(imageResource);
        imageView.setImageDrawable(resource);
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

    public Date createdAt() {
        return plants.getCreationDate(this);
    }

    @Override
    public int compareTo(@NonNull Plant other) {
        return -createdAt().compareTo(other.createdAt());
    }

    public Date getCreationDay() {
        return DateUtils.truncate(createdAt(), Calendar.DAY_OF_MONTH);
    }
}
