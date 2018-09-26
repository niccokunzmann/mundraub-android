package eu.quelltext.mundraub.map.position;

public class Position implements IPosition {

    private final double longitude;
    private final double latitude;

    public Position(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
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
