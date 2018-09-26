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
}
