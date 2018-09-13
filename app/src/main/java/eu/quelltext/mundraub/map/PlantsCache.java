package eu.quelltext.mundraub.map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.api.progress.JoinedProgress;
import eu.quelltext.mundraub.api.progress.Progress;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.plant.PlantCategory;

/*
 * This class is following the tutorial at
 * https://www.androidauthority.com/creating-sqlite-databases-in-your-app-719366/
 */
public class PlantsCache extends ErrorAware {

    private static Context context;
    private static Logger.Log log = Logger.newFor("PlantsCache");
    private static JoinedProgress updateProgress;
    private static final int MAXIMUM_MARKER_COUNT_TO_SERVE = 1000;
    private static final int API_ID_MUNDRAUB = 1;
    private static final int API_ID_NA_OVOCE = 2;

    static {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(Activity context) {
                PlantsCache.context = context;
            }
        });
    }

    static double getLongitude(double longitude) {
        longitude = (longitude + 180) % 360 - 180;
        while (longitude < -180) {
            longitude += 360;
        }
        return longitude;
    }

    static double getLatitude(double latitude) {
        if (latitude > 90 || latitude < -90) {
            log.e("Invalid latitude", latitude + "");
        }
        return latitude;
    }

    public static JSONArray getPlantsInBoundingBox(double minLon, double minLat, double maxLon, double maxLat) throws JSONException {
        minLon = getLongitude(minLon);
        minLat = getLatitude(minLat);
        maxLon = getLongitude(maxLon);
        maxLat = getLatitude(maxLat);
        log.d("minLon", minLon);
        log.d("minLat", minLat);
        log.d("maxLon", maxLon);
        log.d("maxLat", maxLat);
        SQLiteDatabase database = getReadableDatabase();
        try {
            //log.d("number of plants in database", Marker.getCount(database));
            String[] projection = Marker.PROJECTION;

            String selection = "";
            if (minLon < maxLon) {
                selection +=
                        Marker.COLUMN_LONGITUDE + " > " + minLon + " and " +
                                Marker.COLUMN_LONGITUDE + " < " + maxLon;
            } else {
                selection +=
                        Marker.COLUMN_LONGITUDE + " < " + minLon + " and " +
                                Marker.COLUMN_LONGITUDE + " > " + maxLon;
                Log.d("rare case", "minLon " + minLon + " > maxLon" + maxLon);
            }
            if (minLat < maxLat) {
                selection += " and " +
                        Marker.COLUMN_LATITUDE + " > " + minLat + " and " +
                        Marker.COLUMN_LATITUDE + " < " + maxLat;
            } else {
                selection += " and " +
                        Marker.COLUMN_LATITUDE + " < " + minLat + " and " +
                        Marker.COLUMN_LATITUDE + " > " + maxLat;
                Log.d("rare case", "minLat " + minLat + " > maxLat" + maxLat);
            }
            //selection = /*" and " +*/ Marker.COLUMN_TYPE_ID + " = 6";
        /*selection =
                Marker.COLUMN_LONGITUDE + " > ? and " +
                        Marker.COLUMN_LONGITUDE + " < ? and " +
                        Marker.COLUMN_LATITUDE + " > ? and " +
                        Marker.COLUMN_LATITUDE + " < ?" +
                "";*/

            String[] selectionArgs = {
                    Double.toString(minLon),
                    Double.toString(maxLon),
                    Double.toString(minLat),
                    Double.toString(maxLat)
            };
            selectionArgs = null;
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
            JSONArray result = new JSONArray();
            for (int i = 0; i < cursor.getCount() && i < MAXIMUM_MARKER_COUNT_TO_SERVE; i++) {
                cursor.moveToPosition(i);
                Marker marker = Marker.fromCursor(cursor);
                result.put(marker.toJSON());
            }
            return result;
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
            final API[] apis = API.getMarkerAPIs();
            updateProgress = new JoinedProgress(callback, apis.length);
            final List<API.Callback> callbacks = new ArrayList<API.Callback>();
            for (int i = 0; i < apis.length; i++) {
                final int index = i;
                final int nextIndex = i + 1;
                callbacks.add(new API.Callback() {
                    @Override
                    public void onSuccess() {
                        API api = apis[index];
                        API.Callback nextCallback = callbacks.get(nextIndex);
                        updateProgress.addProgressable(api.updateAllPlantMarkers(nextCallback));
                    }

                    @Override
                    public void onFailure(int errorResourceString) {
                        onSuccess();
                    }
                });
            }
            callbacks.add(API.Callback.NULL);
            callbacks.get(0).onSuccess();
        } else {
            updateProgress.addCallback(callback);
        }
        return updateProgress;
    }

    public static Progress getUpdateProgressOrNull() {
        return updateProgress;
    }



    public static class Marker implements BaseColumns {
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

        private final double longitude;
        private final double latitude;
        private final PlantCategory category;
        private final int markerIdInAPI;
        private final int apiId;

        private Marker(double longitude, double latitude, PlantCategory category, int markerIdInAPI, int apiId) {
            this.longitude = getLongitude(longitude);
            this.latitude = getLatitude(latitude);
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

        public static int getCount(SQLiteDatabase database) {
            return database.rawQuery("SELECT " + _ID  + " from " + TABLE_NAME, null).getCount();
        }

        public static int getCount() {
            SQLiteDatabase database = getReadableDatabase();
            try {
                return getCount(database);
            } finally {
                database.close();
            }
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

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            JSONObject properties = new JSONObject();
            JSONArray position = new JSONArray();
            position.put(latitude); // latitude before longitude
            position.put(longitude);
            json.put(JSON_MUNDRAUB_POSITION, position);
            properties.put(JSON_MUNDRAUB_TYPE_ID, category.getValueForMundraubAPI());
            properties.put(JSON_MUNDRAUB_NODE_ID, markerIdInAPI);
            json.put(JSON_MUNDRAUB_PROPERTIES, properties);
            return json;
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
                            PlantCategory.fromNaOvoceAPIField(Integer.parseInt(markerJSON.getString(JSON_NA_OVOCE_KIND), 16)),
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
            log.d("markers in database", Marker.getCount(database));
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
