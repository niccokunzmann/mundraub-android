package eu.quelltext.mundraub.map.position;

public class Position implements IPosition {

    private final double longitude;
    private final double latitude;

    public Position(double longitude, double latitude) {
        this.longitude = BoundingBox.getLongitude(longitude);
        this.latitude = BoundingBox.getLatitude(latitude);
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public int hashCode() {
        return (int)(Math.round(longitude * 10000) ^ Math.round(latitude * 10000));
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return super.equals(obj);
        }
        Position other = (Position) obj;
        return getLongitude() == other.getLongitude() && getLatitude() == other.getLatitude();
    }
}
