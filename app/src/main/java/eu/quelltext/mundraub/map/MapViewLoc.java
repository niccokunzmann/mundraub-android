package eu.quelltext.mundraub.map;

import android.content.Context;
import android.graphics.Canvas;
import android.location.Criteria;
import android.location.Location;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MapViewLoc extends MapView {

    private Overlay tapOverlay;
    private OnTapListener onTapListener;

    protected MapViewLoc(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase tileProvider, Handler tileRequestCompleteHandler, AttributeSet attrs) {
        super(context, tileSizePixels, resourceProxy, tileProvider, tileRequestCompleteHandler, attrs);
    }

    public MapViewLoc(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapViewLoc(Context context, int tileSizePixels) {
        super(context, tileSizePixels);
    }

    public MapViewLoc(Context context, int tileSizePixels, ResourceProxy resourceProxy) {
        super(context, tileSizePixels, resourceProxy);
    }

    public MapViewLoc(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
        super(context, tileSizePixels, resourceProxy, aTileProvider);
    }

    public MapViewLoc(Context context, int tileSizePixels, ResourceProxy resourceProxy, MapTileProviderBase aTileProvider, Handler tileRequestCompleteHandler) {
        super(context, tileSizePixels, resourceProxy, aTileProvider, tileRequestCompleteHandler);
    }

    private void prepareTagOverlay(){

        this.tapOverlay = new Overlay(this.getContext()) {

            @Override
            protected void draw(Canvas c, MapView osmv, boolean shadow) {

            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {

                Projection proj = mapView.getProjection();
                GeoPoint p = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                proj = mapView.getProjection();

                final GeoPoint geoPoint = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());

                if(MapViewLoc.this.onTapListener != null){

                    MapViewLoc.this.onTapListener.onMapTapped(geoPoint);

                    Location location = new Location("");
                    location.setLatitude((double) geoPoint.getLatitudeE6() / 1000000);
                    location.setLongitude((double) geoPoint.getLongitudeE6() / 1000000);
                    location.setAccuracy(Criteria.ACCURACY_FINE);

                    MapViewLoc.this.onTapListener.onMapTapped(location);
                }

                return true;
            }
        };
    }

    public void addTapListener(OnTapListener onTapListener){

        this.prepareTagOverlay();

        this.getOverlays().add(0, this.tapOverlay);

        this.onTapListener = onTapListener;
    }

    public void removeTapListener(){

        if(this.tapOverlay != null && this.getOverlays().size() > 0){

            this.getOverlays().remove(0);
        }

        this.tapOverlay = null;
        this.onTapListener = null;
    }

    public interface OnTapListener{

        void onMapTapped(GeoPoint geoPoint);

        void onMapTapped(Location location);

    }

}