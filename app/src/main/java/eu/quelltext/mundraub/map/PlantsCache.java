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

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;

/*
 * This class is following the tutorial at
 * https://www.androidauthority.com/creating-sqlite-databases-in-your-app-719366/
 */
public class PlantsCache extends ErrorAware {

    private static Context context;
    private static Logger.Log log = Logger.newFor("PlantsCache");

    static {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(Activity context) {
                context = context;
            }
        });
    }

    public static JSONArray getPlantsInBoundingBox(double minLon, double minLat, double maxLon, double maxLat) throws JSONException {
        SQLiteDatabase database = getReadableDatabase();

        String[] projection = {
                Marker.COLUMN_LONGITUDE,
                Marker.COLUMN_LATITUDE,
                Marker.COLUMN_TYPE_ID,
                Marker.COLUMN_NODE_ID
        };

        String selection =
                Marker.COLUMN_LONGITUDE + " >= ? and " +
                Marker.COLUMN_LONGITUDE + " <= ? and " +
                Marker.COLUMN_LATITUDE  + " >= ? and " +
                Marker.COLUMN_LATITUDE  + " <= ?";

        String[] selectionArgs = {
                Double.toString(minLon),
                Double.toString(maxLon),
                Double.toString(minLat),
                Double.toString(maxLat)
        };

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
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            JSONObject marker = new JSONObject();
            JSONArray position = new JSONArray();
            marker.put(JSON_TYPE_ID, cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_TYPE_ID)));
            marker.put(JSON_NODE_ID, cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_NODE_ID)));
            position.put(cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_LATITUDE)));
            position.put(cursor.getDouble(cursor.getColumnIndexOrThrow(Marker.COLUMN_LONGITUDE)));
            marker.put(JSON_POSITION, position);
            result.put(marker);
        }
        return result;
    }

    public static void clear() {
        new MarkerDBSQLiteHelper().clearTable(getWritableDatabase());
    }

    public static class Marker implements BaseColumns {
        public static final String TABLE_NAME = "marker";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_TYPE_ID = "type";
        public static final String COLUMN_NODE_ID = "node";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LONGITUDE + " DOUBLE, " +
                COLUMN_LATITUDE + " DOUBLE, " +
                COLUMN_TYPE_ID + " INTEGER, " +
                COLUMN_NODE_ID + " INTEGER" + ")";

        private final double longitude;
        private final double latitude;
        private final int type;
        private final int node;

        private Marker(double longitude, double latitude, int type, int node) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = type;
            this.node = node;
        }

        private void saveToDB(SQLiteDatabase database) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_LATITUDE,  latitude);
            values.put(COLUMN_TYPE_ID,   type);
            values.put(COLUMN_NODE_ID,   node);
            /*long rowId = */database.insert(TABLE_NAME, null, values);
        }
    }

    public static class MarkerDBSQLiteHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "marker_database";

        public MarkerDBSQLiteHelper() {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    private static final String JSON_FEATURES = "features";
    private static final String JSON_POSITION = "position";
    private static final String JSON_TYPE_ID = "tid";
    private static final String JSON_NODE_ID = "nid";
    private static final int JSON_INDEX_LONGITUDE = 1;
    private static final int JSON_INDEX_LATITUDE = 0;

    public static void updatePlantMarkers(JSONObject json) throws API.ErrorWithExplanation {
        // this is called form the API with all markers needed.
        SQLiteDatabase database = getWritableDatabase();
        new MarkerDBSQLiteHelper().clearTable(database);
        if (json == null || !json.has(JSON_FEATURES)) {
            return;
        }
        // see the api for the response
        // https://github.com/niccokunzmann/mundraub-android/blob/master/docs/api.md#markers
        try {
            JSONArray markers = json.getJSONArray(JSON_FEATURES);
            for (int i = 0; i < markers.length(); i++) {
                JSONObject markerJSON = markers.getJSONObject(i);
                JSONArray position = markerJSON.getJSONArray(JSON_POSITION);
                Marker marker = new Marker(
                        position.getDouble(JSON_INDEX_LONGITUDE),
                        position.getDouble(JSON_INDEX_LATITUDE),
                        Integer.parseInt(markerJSON.getString(JSON_TYPE_ID)),
                        Integer.parseInt(markerJSON.getString(JSON_NODE_ID))
                        );
                marker.saveToDB(database);
            }
        } catch (JSONException e) {
            log.printStackTrace(e);
            API.abortOperation(R.string.error_invalid_json_for_markers);
            return;
        }
    }

    private static SQLiteDatabase getWritableDatabase() {
        return new MarkerDBSQLiteHelper().getWritableDatabase();
    }

    private static SQLiteDatabase getReadableDatabase() {
        return new MarkerDBSQLiteHelper().getReadableDatabase();
    }
}
