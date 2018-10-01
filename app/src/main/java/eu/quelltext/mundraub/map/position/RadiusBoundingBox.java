package eu.quelltext.mundraub.map.position;

import java.util.List;

import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.map.PlantsCache;

public class RadiusBoundingBox extends BoundingBox {

    private final double radiusInMeters;
    private final IPosition center;
    private final double deltaDegrees;

    public static BoundingBox fromPositionAndRadius(IPosition position, double distanceInMeters) {
        double deltaDegrees = Helper.metersToDegrees(distanceInMeters);
        return new RadiusBoundingBox(position, distanceInMeters, deltaDegrees);
    }

    private RadiusBoundingBox(IPosition center, double radiusInMeters, double deltaDegrees) {
        super(
                center.getLatitude() + deltaDegrees,
                center.getLongitude() + deltaDegrees,
                center.getLatitude() - deltaDegrees,
                center.getLongitude() - deltaDegrees);
        this.radiusInMeters = radiusInMeters;
        this.deltaDegrees = deltaDegrees;
        this.center = center;
    }

    @Override
    public List<PlantsCache.Marker> selectPositionsInsideAfterSQLQuery(List<PlantsCache.Marker> positions) {
        positions = super.selectPositionsInsideAfterSQLQuery(positions);
        for (int i = positions.size() - 1; i >= 0; i--) {
            PlantsCache.Marker marker = positions.get(i);
            double distanceInMeters = distanceInMetersBetween(marker, center);
            if (distanceInMeters > radiusInMeters) {
                positions.remove(i);
            }
        }
        return positions;
    }

    @Override
    public boolean contains(Position position) {
        // TODO: test
        return super.contains(position) && distanceInMetersBetween(position, center) < radiusInMeters;
    }
}
