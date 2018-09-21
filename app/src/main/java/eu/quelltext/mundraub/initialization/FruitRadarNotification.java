package eu.quelltext.mundraub.initialization;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.MundraubBaseActivity;
import eu.quelltext.mundraub.activities.map.ShowPlantsActivity;
import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.map.PlantsCache;
import eu.quelltext.mundraub.plant.Plant;

public class FruitRadarNotification extends ErrorAware {

    private static MundraubBaseActivity activity;
    private static FruitRadarNotification instance;
    private static final String CHANNEL_ID_PLANTS_NEABY = "PLANTS_NEARBY";
    private static int lastCreatedNotificationId = 0;
    private Vibrator vibrator;
    private double[] lastPosition = new double[]{0, 0};

    static void initialize() {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(Activity context) {
                activity = (MundraubBaseActivity)context;
                if (Settings.useFruitRadarNotifications()) {
                    instance().start();
                }
                Settings.onChange(new Settings.ChangeListener() {
                    @Override
                    public int settingsChanged() {
                        if (Settings.useFruitRadarNotifications()) {
                            instance().start();
                        } else {
                            instance().stop();
                        }
                        return Settings.COMMIT_SUCCESSFUL;
                    }
                });
            }
        });
    }

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private Map<PlantsCache.Marker, Notification> markerToNotification = new HashMap<>();
    private final NotificationManagerCompat notificationManager;

    private static FruitRadarNotification instance() {
        if (instance == null) {
            instance = new FruitRadarNotification();
        }
        return instance;
    }

    public void start() {
        if (isStarted()) {
            return;
        }
        if (getLocationManager() != null) {
            startGPSUpdates();
        }
    }

    private void vibrate() {
        if (!Settings.vibrateWhenPlantIsInRange()) {
            return;
        }
        // for vibration, see https://stackoverflow.com/a/13950364/1320237
        if (vibrator == null) {
            vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null) {
            // TODO: vibration is deprecated like this from Build.VERSION.SDK_INT >= 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(Settings.vibrationMillisecondsForPlantInRange());
                }
            } else {
                vibrator.vibrate(Settings.vibrationMillisecondsForPlantInRange());
            }
        }
    }

    public void stop() {
        if (isStarted()) {
            locationManager.removeUpdates(locationListener);
            log.d("GPS", "stopped");
        }
        locationManager = null;
        locationListener = null;
    }

    private LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = activity.createLocationManager();
        }
        return locationManager;
    }

    public boolean isStarted() {
        return locationListener != null;
    }

    @SuppressLint("MissingPermission") // this is checked in the activity
    private void startGPSUpdates() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                FruitRadarNotification.this.onLocationChanged(location.getLongitude(), location.getLatitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        getLocationManager().requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, Settings.getRadarGPSPrecisionMeters(), locationListener);
        log.d("GPS", "started");
    }

    private void onLocationChanged(double longitude, double latitude) {
        boolean vibrated = false;
        lastPosition = new double[]{longitude, latitude};
        List<PlantsCache.Marker> markers = PlantsCache.getMarkersInRangeMeters(
                longitude, latitude, Settings.getRadarPlantRangeMeters());
        Map<PlantsCache.Marker, Notification> oldMarkerToNotification = markerToNotification;
        Map<PlantsCache.Marker, Notification> newMarkerToNotification = new HashMap<>();
        for (PlantsCache.Marker marker : markers) {
            Notification notification = oldMarkerToNotification.get(marker);
            if (notification == null) {
                notification = new Notification(marker, longitude, latitude);
                if (!vibrated) {
                    vibrate();
                    vibrated = true;
                }
            } else {
                notification.updateLocation(longitude, latitude);
            }
            newMarkerToNotification.put(marker, notification);
            oldMarkerToNotification.remove(marker);
        }
        for (Notification notification : oldMarkerToNotification.values()) {
            if (notification.getDistanceInMeters() > Settings.getRadarPlantMaximumRangeMeters() * 1.5) {
                notification.delete();
            } else {
                newMarkerToNotification.put(notification.getMarker(), notification);
            }
        }
    }

    private FruitRadarNotification() {
        notificationManager = NotificationManagerCompat.from(activity);
    }

    public static void showExample() {
        instance().showExampleNotification();
    }

    private void showExampleNotification() {
        lastPosition = Plant.getAPositionNearAPlantForTheMap().toArray();
        PlantsCache.Marker marker = PlantsCache.Marker.example(lastPosition);
        new Notification(marker, lastPosition[0], lastPosition[1]);
        vibrate();
    }

    /*
     * This is a state holder for the notifications provided by Android
     * see
     * - https://developer.android.com/guide/topics/ui/notifiers/notifications
     * - https://developer.android.com/training/notify-user/build-notification
     */
    class Notification {

        private final PlantsCache.Marker marker;
        private final int id;
        private double distanceInMeters;

        public Notification(PlantsCache.Marker marker, double longitude, double latitude) {
            this.marker = marker;
            distanceInMeters = distanceInMetersTo(longitude, latitude);
            id = ++lastCreatedNotificationId;

            notifyUser();
        }

        private void notifyUser() {
            Intent intent = new Intent(activity, ShowPlantsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(ShowPlantsActivity.ARG_POSITION, lastPosition);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity)
                    .setSmallIcon(marker.getNotificationIcon())
                    .setContentTitle(activity.getString(marker.getResourceId()))
                    .setContentText(getText())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true);
            notificationManager.notify(id, mBuilder.build());
        }

        @SuppressLint("StringFormatInvalid")
        private String getText() {
            return String.format(
                    activity.getString(R.string.notification_text_with_distance),
                    Math.round(distanceInMeters));
        }

        private double distanceInMetersTo(double longitude, double latitude) {
            return Helper.distanceInMetersBetween(
                    marker.getLongitude(),
                    marker.getLatitude(),
                    longitude,
                    latitude);
        }

        /* called when the location changed */
        public void updateLocation(double longitude, double latitude) {
            distanceInMeters = distanceInMetersTo(longitude, latitude);
            notifyUser();
        }

        public void delete() {
            notificationManager.cancel(id);
        }

        public double getDistanceInMeters() {
            return distanceInMeters;
        }

        public PlantsCache.Marker getMarker() {
            return marker;
        }
    }

}
