package eu.quelltext.mundraub.map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.api.BackgroundDownloadTask;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.IPosition;
import eu.quelltext.mundraub.plant.PlantCategory;

/*
 * This class is following the tutorial at
 * https://www.androidauthority.com/creating-sqlite-databases-in-your-app-719366/
 */
public class PlantsCache extends ErrorAware {

    private static Context context;
    private static Logger.Log log = Logger.newFor("PlantsCache");
    private static Progress updateProgress;
    private static final int API_ID_EXAMPLE = 0;
    private static final int API_ID_MUNDRAUB = 1;
    private static final int API_ID_NA_OVOCE = 2;
    private static final int API_ID_FRUITMAP = 3;

    static {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(Activity context) {
                PlantsCache.context = context;
            }
        });
    }

    public static JSONArray getPlantsInBoundingBox(BoundingBox bbox) throws JSONException {
        List<Marker> markers = getMarkersInBoundingBox(bbox);
        JSONArray result = new JSONArray();
        for (Marker marker: markers) {
            result.put(marker.toJSON());
        }
        return result;
    }

    public static List<Marker> getMarkersInBoundingBox(BoundingBox bbox) {
        SQLiteDatabase database = getReadableDatabase();
        try {
            //log.d("number of plants in database", Marker.getCount(database));
            String[] projection = Marker.PROJECTION;

            String selection = bbox.asSqlQueryString(Marker.COLUMN_LONGITUDE, Marker.COLUMN_LATITUDE);

            String[] selectionArgs = null;
            //selection = null;

            Cursor cursor = database.query(
                    Marker.TABLE_NAME,  // The table to query
                    projection,         // The columns to return
                    selection,          // The columns for the WHERE clause
                    selectionArgs,      // The values for the WHERE clause
                    null,       // don't group the rows
                    null,        // don't filter by row groups
                    null        // don't sort
            );
            log.d("getPlantsInBoundingBox", "The total cursor count is " + cursor.getCount());
            List<Marker> result = new ArrayList<>();
            int max = cursor.getCount();
			int maxDisplayed = Settings.getMaximumDisplayedMarkers();
            for (int i = 0; i < max && i < maxDisplayed; i++) {
                cursor.moveToPosition(i);
                Marker marker = Marker.fromCursor(cursor);
                result.add(marker);
            }
            return bbox.selectPositionsInsideAfterSQLQuery(result);
        } finally {
            database.close();
        }
    }

    public static void clear() {
        SQLiteDatabase database = getWritableDatabase();
        try {
            new MarkerDBSQLiteHelper().clearTable(database);
        } finally {
            database.close();
        }
    }

    public static Progress update(API.Callback callback) {
        if (updateProgress == null || updateProgress.isDone()) {
            clear();
            BackgroundDownloadTask task = new BackgroundDownloadTask();
            for (API api :API.getMarkerAPIs()) {
                task.collectDownloadsFrom(api);
            }
            task.downloadInBackground(callback);
            updateProgress = task.getProgress();
        } else {
            updateProgress.addCallback(callback);
        }
        return updateProgress;
    }

    public static Progress getUpdateProgressOrNull() {
        return updateProgress;
    }

    public static class Marker implements BaseColumns, IPosition {
        public static final String TABLE_NAME = "marker";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_MARKER_ID_IN_API = "node";
        public static final String COLUMN_CATEGORY_ID = "category";
        public static final String COLUMN_API_ID = "api";
        public static final String[] PROJECTION = {
                COLUMN_LONGITUDE,
                COLUMN_LATITUDE,
                COLUMN_MARKER_ID_IN_API,
                COLUMN_CATEGORY_ID,
                COLUMN_API_ID
        };

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LONGITUDE + " DOUBLE, " +
                COLUMN_LATITUDE + " DOUBLE, " +
                COLUMN_MARKER_ID_IN_API + " INTEGER, " +
                COLUMN_CATEGORY_ID + " TINYINT, " +
                COLUMN_API_ID + " TINYINT" +
                ")";
        private static final int INVALID_COUNT = -1;
        private static int totalCountInDatabase = INVALID_COUNT;

        private final double longitude;
        private final double latitude;
        private final PlantCategory category;
        private final int markerIdInAPI;
        private final int apiId;

        private Marker(double longitude, double latitude, PlantCategory category, int markerIdInAPI, int apiId) {
            this.longitude = BoundingBox.getLongitude(longitude);
            this.latitude = BoundingBox.getLatitude(latitude);
            this.category = category;
            this.markerIdInAPI = markerIdInAPI;
            this.apiId = apiId;
        }

        private void saveToDB(SQLiteDatabase database) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_LATITUDE,  latitude);
            values.put(COLUMN_MARKER_ID_IN_API,   markerIdInAPI);
            values.put(COLUMN_CATEGORY_ID,   category.getDatabaseId());
            values.put(COLUMN_API_ID,   apiId);
            /*long rowId = */database.insert(TABLE_NAME, null, values);
        }

        public static void updateCount(SQLiteDatabase database) {
            totalCountInDatabase = database.rawQuery("SELECT " + _ID  + " from " + TABLE_NAME, null).getCount();
        }

        public static int getCount() {
            if (totalCountInDatabase == INVALID_COUNT) {
                SQLiteDatabase database = getReadableDatabase();
                try {
                    updateCount(database);
                } finally {
                    database.close();
                }
            }
            return totalCountInDatabase;
        }

        public static Marker fromCursor(Cursor cursor) {
            return new Marker(
                    cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_LONGITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_LATITUDE)),
                    PlantCategory.fromDatabaseId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MARKER_ID_IN_API)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_API_ID))
            );
        }

        private static final String JSON_MAP_CATEGORY = "category";

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            JSONObject properties = new JSONObject();
            JSONArray position = new JSONArray();
            position.put(latitude); // latitude before longitude
            position.put(longitude);
            json.put(JSON_MUNDRAUB_POSITION, position);
            //properties.put(JSON_MUNDRAUB_TYPE_ID, category.getValueForMundraubAPI());
            properties.put(JSON_MAP_CATEGORY, category.getId());
            properties.put(JSON_MUNDRAUB_NODE_ID, markerIdInAPI);
            json.put(JSON_MUNDRAUB_PROPERTIES, properties);
            return json;
        }

        public double distanceInMetersTo(double longitude, double latitude) {
            return Helper.distanceInMetersBetween(this.longitude, this.latitude, longitude, latitude);
        }

        @Override
        public int hashCode() {
            return this.apiId ^ this.markerIdInAPI;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Marker)) {
                return super.equals(obj);
            }
            Marker other = (Marker) obj;
            return other.apiId == this.apiId && other.markerIdInAPI == this.markerIdInAPI;
        }

        public double getLongitude() {
            return longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public int getNotificationIcon() {
            return R.mipmap.ic_launcher_round;
        }

        public int getResourceId() {
            return category.getResourceId();
        }

        public static Marker example(double[] position) {
            return new Marker(position[0],position[1], PlantCategory.EXAMPLE, 1, API_ID_EXAMPLE);
        }

        public PlantCategory getCategory() {
            return category;
        }
    }

    public static class MarkerDBSQLiteHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "marker_database.db";

        public MarkerDBSQLiteHelper() {
            super(PlantsCache.context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(Marker.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            clearTable(sqLiteDatabase);
        }

        private void clearTable(SQLiteDatabase sqLiteDatabase) {
            dropTable(sqLiteDatabase);
            onCreate(sqLiteDatabase);
        }

        private void dropTable(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Marker.TABLE_NAME);
        }
    }

    private static final String JSON_MUNDRAUB_FEATURES = "features";
    private static final String JSON_MUNDRAUB_POSITION = "pos";
    private static final String JSON_MUNDRAUB_TYPE_ID = "tid";
    private static final String JSON_MUNDRAUB_NODE_ID = "nid";
    private static final String JSON_MUNDRAUB_PROPERTIES = "properties";
    private static final int JSON_MUNDRAUB_INDEX_LONGITUDE = 1;
    private static final int JSON_MUNDRAUB_INDEX_LATITUDE = 0;

    public static void updateMundraubPlantMarkers(JSONObject json, Progressable fraction) throws API.ErrorWithExplanation {
        // this is called form the API with all markers needed.
        if (json == null || !json.has(JSON_MUNDRAUB_FEATURES)) {
            return;
        }

        try {
            JSONArray markers = json.getJSONArray(JSON_MUNDRAUB_FEATURES);
            JSONObject invalidMarker = null;
            // see the api for the response
            // https://github.com/niccokunzmann/mundraub-android/blob/master/docs/api.md#markers
            BulkWriter writer = new BulkWriter(getWritableDatabase(), fraction, markers.length());
            try {
                for (int i = 0; i < markers.length(); i++) {
                    JSONObject markerJSON = markers.getJSONObject(i);
                    if (!markerJSON.has(JSON_MUNDRAUB_POSITION) || !markerJSON.has(JSON_MUNDRAUB_PROPERTIES)) {
                        invalidMarker = markerJSON;
                        continue;
                    }
                    JSONArray position = markerJSON.getJSONArray(JSON_MUNDRAUB_POSITION);
                    JSONObject properties = markerJSON.getJSONObject(JSON_MUNDRAUB_PROPERTIES);
                    if (!properties.has(JSON_MUNDRAUB_TYPE_ID) || !properties.has(JSON_MUNDRAUB_NODE_ID) ||
                            position.length() != 2) {
                        invalidMarker = markerJSON;
                        continue;
                    }
                    writer.addMarker(
                            position.getDouble(JSON_MUNDRAUB_INDEX_LONGITUDE),
                            position.getDouble(JSON_MUNDRAUB_INDEX_LATITUDE),
                            PlantCategory.fromMundraubAPIField(Integer.parseInt(properties.getString(JSON_MUNDRAUB_TYPE_ID))),
                            Integer.parseInt(properties.getString(JSON_MUNDRAUB_NODE_ID)),
                            API_ID_MUNDRAUB
                    );
                }
                writer.success();
            } finally {
                writer.close();
            }
            if (invalidMarker != null) {
                log.e("invalidMarker", invalidMarker.toString());
            }
        } catch (JSONException e) {
            log.printStackTrace(e);
            API.abortOperation(R.string.error_invalid_json_for_markers);
            return;
        }

    }

    private static final String JSON_NA_OVOCE_LONGITUDE = "lng";
    private static final String JSON_NA_OVOCE_LATITUDE = "lat";
    private static final String JSON_NA_OVOCE_KIND = "kind";
    private static final String JSON_NA_OVOCE_ID = "id";


    public static void updateNaOvocePlantMarkers(JSONArray markers, Progressable fraction) throws API.ErrorWithExplanation {
        // example from https://na-ovoce.cz/api/v1/fruit/?kind=a1bb
        // [{"id":39634,"lat":"48.6093236745","lng":"17.9082361616","kind":"a1bb","time":"2018-08-31 13:21:34","url":"https://na-ovoce.cz/api/v1/fruit/39634/"}, ...
        try {
            JSONObject invalidMarker = null;
            // see the api for the response
            // https://github.com/niccokunzmann/mundraub-android/blob/master/docs/api.md#markers
            BulkWriter writer = new BulkWriter(getWritableDatabase(), fraction, markers.length());
            try {
                for (int i = 0; i < markers.length(); i++) {
                    JSONObject markerJSON = markers.getJSONObject(i);
                    if (    !markerJSON.has(JSON_NA_OVOCE_LONGITUDE) ||
                            !markerJSON.has(JSON_NA_OVOCE_LATITUDE) ||
                            !markerJSON.has(JSON_NA_OVOCE_KIND) ||
                            !markerJSON.has(JSON_NA_OVOCE_ID)) {
                        invalidMarker = markerJSON;
                        continue;
                    }
                    writer.addMarker(
                            markerJSON.getDouble(JSON_NA_OVOCE_LONGITUDE),
                            markerJSON.getDouble(JSON_NA_OVOCE_LATITUDE),
                            PlantCategory.fromNaOvoceAPIField(markerJSON.getString(JSON_NA_OVOCE_KIND)),
                            Integer.parseInt(markerJSON.getString(JSON_NA_OVOCE_ID)),
                            API_ID_NA_OVOCE
                    );
                }
                writer.success();
            } finally {
                writer.close();
            }
            if (invalidMarker != null) {
                log.e("invalidMarker", invalidMarker.toString());
            }
        } catch (JSONException e) {
            log.printStackTrace(e);
            API.abortOperation(R.string.error_invalid_json_for_markers);
            return;
        }
    }

    private static final String JSON_FRUITMAP_FRUIT = "fruit";
    private static final String JSON_FRUITMAP_LONGITUDE = "y";
    private static final String JSON_FRUITMAP_LATITUDE = "x";
    private static final String JSON_FRUITMAP_USER = "user";
    private static final String JSON_FRUITMAP_ID = "id";
    private static final String JSON_FRUITMAP_TYPE = "nazov";

    public static void updateFruitMapPlantMarkers(JSONObject json, Progressable fraction) throws API.ErrorWithExplanation {
        // example from
        // {"count":46,"fruit":[{"id":"92","tree":"11","x":"48.7345100000","y":"19.1145800000","user":"1","image":"","status":"1","deleted_reason":null,"meta_ref_id":"27698","created_at":"1274542713","updated_at":"1274542713","nazov":"Red ribes"},{"id":"170","tree":" ...
        try {
            JSONArray markers = json.getJSONArray(JSON_FRUITMAP_FRUIT);
            JSONObject invalidMarker = null;
            // see the api for the response
            // https://github.com/niccokunzmann/mundraub-android/blob/master/docs/api.md#markers
            BulkWriter writer = new BulkWriter(getWritableDatabase(), fraction, markers.length());
            try {
                for (int i = 0; i < markers.length(); i++) {
                    JSONObject markerJSON = markers.getJSONObject(i);
                    if (    !markerJSON.has(JSON_FRUITMAP_LONGITUDE) ||
                            !markerJSON.has(JSON_FRUITMAP_LATITUDE) ||
                            !markerJSON.has(JSON_FRUITMAP_USER) ||
                            !markerJSON.has(JSON_FRUITMAP_TYPE) ||
                            !markerJSON.has(JSON_FRUITMAP_ID)) {
                        invalidMarker = markerJSON;
                        continue;
                    }
                    String user = markerJSON.getString(JSON_FRUITMAP_USER);
                    if (!user.equals("301" /*301 = na ovoce*/)) {
                        writer.addMarker(
                                markerJSON.getDouble(JSON_FRUITMAP_LONGITUDE),
                                markerJSON.getDouble(JSON_FRUITMAP_LATITUDE),
                                PlantCategory.fromFruitMapAPIField(markerJSON.getString(JSON_FRUITMAP_TYPE)),
                                Integer.parseInt(markerJSON.getString(JSON_FRUITMAP_ID)),
                                API_ID_FRUITMAP
                        );
                    }
                }
                writer.success();
            } finally {
                writer.close();
            }
            if (invalidMarker != null) {
                log.e("invalidMarker", invalidMarker.toString());
            }
        } catch (JSONException e) {
            log.printStackTrace(e);
            API.abortOperation(R.string.error_invalid_json_for_markers);
            return;
        }
    }



    private static class BulkWriter {

        private static final int BULK_INSERT_MARKERS = 500;

        private final SQLiteDatabase database;
        private final Progressable fraction;
        private final int totalCount;
        private int markersAdded = 0;

        private BulkWriter(SQLiteDatabase database, Progressable fraction, int totalCount) {
            this.database = database;
            this.fraction = fraction;
            this.totalCount = totalCount;
            log.d("number of markers to add", totalCount);
            database.beginTransaction();
        }

        public void success() {
            database.setTransactionSuccessful(); // from https://stackoverflow.com/a/32088155
            Marker.updateCount(database);
            log.d("markers in database", Marker.getCount());
        }

        public void close() {
            database.endTransaction();
            database.close();
            log.d("markers added", markersAdded);
        }

        public void addMarker(double longitude, double latitude, PlantCategory category, int markerIdInAPI, int apiId) {
            Marker marker = new Marker(longitude, latitude, category, markerIdInAPI, apiId);
            marker.saveToDB(database);
            fraction.setProgress(1.0 * markersAdded / totalCount);
            markersAdded++;
            if (markersAdded % BULK_INSERT_MARKERS == 0) {
                database.setTransactionSuccessful();
                database.endTransaction();
                log.d("bulk insert markers", BULK_INSERT_MARKERS + " " + markersAdded + " of " + totalCount);
                database.beginTransaction();
            }

        }
    }

    private static SQLiteDatabase getWritableDatabase() {
        return new MarkerDBSQLiteHelper().getWritableDatabase();
    }

    private static SQLiteDatabase getReadableDatabase() {
        return new MarkerDBSQLiteHelper().getReadableDatabase();
    }


}
