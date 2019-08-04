package eu.quelltext.mundraub.map.position;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.map.PlantsCache;

public class BoundingBox {
    private final double north;
    private final double south;
    private final double east;
    private final double west;

    protected BoundingBox(double north, double east, double south, double west) {
        this.north = getLatitude(north); // latitude
        this.south = getLatitude(south); // latitude
        this.east = getLongitude(east); // longitude
        this.west = getLongitude(west); // longitude
    }

    public static BoundingBox fromWestSouthEastNorthArray(String[] bbox) {
        return fromWSEN(
                Double.parseDouble(bbox[0]),
                Double.parseDouble(bbox[1]),
                Double.parseDouble(bbox[2]),
                Double.parseDouble(bbox[3]));
    }
    public static BoundingBox fromWSEN(double west, double south, double east, double north) {
        return new BoundingBox(north, east, south, west);
    }


    public static BoundingBox fromPositionAndRadius(IPosition position, double distanceInMeters) {
        return RadiusBoundingBox.fromPositionAndRadius(position, distanceInMeters);
    }

    public static double getLongitude(double longitude) {
        longitude = (longitude + 180) % 360 - 180;
        while (longitude < -180) {
            longitude += 360;
        }
        return longitude;
    }

    public static double getLatitude(double latitude) {
        if (latitude > 90) {
            return 90;
        }
        if (latitude < -90) {
            return -90;
        }
        return latitude;
    }

    public String asSqlQueryString(String columnLongitude, String columnLatitude) {
        return  " ( " + columnLongitude + " < " + east + (east < west ? " or " : " and ") + columnLongitude + " > " + west + " ) " +
                " and " +
                " ( " + columnLatitude + " > " + south + " and " + columnLatitude + " < " + north + ")";
    }

    public List<PlantsCache.Marker> selectPositionsInsideAfterSQLQuery(List<PlantsCache.Marker> positions) {
        return positions;
    }

    public static double distanceInMetersBetween(IPosition p1, IPosition p2) {
        return Helper.distanceInMetersBetween(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude());
    }

    public static BoundingBox fromNESW(double north, double east, double south, double west) {
        return new BoundingBox(north, east, south, west);
    }

    public boolean contains(Position position) {
        double lon = position.getLongitude();
        double lat = position.getLatitude();
        // this should be equivalent to the `asSqlQueryString()`
        boolean v1 = (lon < east);
        boolean v2 = (lon > west);
        return (east < west ? v1 || v2 : v1 && v2) && (lat > south && lat < north);
    }

    public Position middle() {
        return new Position((east + west) / 2, (north + south) / 2);
    }

    public static BoundingBox ofTileAt(TilePosition position) {
        return new BoundingBox(
                tile2lat(position.y(), position.zoom()),
                tile2lon(position.x() + 1, position.zoom()),
                tile2lat(position.y() + 1, position.zoom()),
                tile2lon(position.x(), position.zoom()));
    }

    static double tile2lon(int x, int z) {
        // from https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static double tile2lat(int y, int z) {
        // from https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    public double southBorderLatitude() {
        return south;
    }

    public double northBorderLatitude() {
        return north;
    }

    public Position southWestCorner() {
        return new Position(west, south);
    }

    public Position southEastCorner() {
        return new Position(east, south);
    }

    public Position northWestCorner() {
        return new Position(west, north);
    }

    public Position northEastCorner() {
        return new Position(east, north);
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return super.equals(obj);
        }
        BoundingBox other = (BoundingBox) obj;
        return other.northEastCorner().equals(this.northEastCorner()) && other.southWestCorner().equals(this.southWestCorner());
    }

    @Override
    public int hashCode() {
        return northEastCorner().hashCode() ^ southWestCorner().hashCode();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("south", southBorderLatitude());
            json.put("north", northBorderLatitude());
            json.put("east", eastBorderLongitude());
            json.put("west", westBorderLongitude());
        } catch (JSONException e) {
            e.printStackTrace(); // will never happen I suppose
        }
        return json;
    }

    private double westBorderLongitude() {
        return west;
    }

    private double eastBorderLongitude() {
        return east;
    }

    public static BoundingBox fromJSON(JSONObject json) throws JSONException {
        return fromNESW(
                json.getDouble("north"),
                json.getDouble("east"),
                json.getDouble("south"),
                json.getDouble("west")
        );
    }

    public double deltaLongitude() {
        if (crosses180()) {
            return 360 + east - west;
        }
        return east - west;
    }

    public boolean crosses180() {
        return east < west;
    }

    public String toExtentString() {
        return Double.toString(west) + "," +
                Double.toString(south) + "," +
                Double.toString(east) + "," +
                Double.toString(north);
    }
}
