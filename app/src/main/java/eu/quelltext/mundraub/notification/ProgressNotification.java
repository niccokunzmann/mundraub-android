package eu.quelltext.mundraub.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.MundraubBaseActivity;
import eu.quelltext.mundraub.api.progress.ProgressableResult;

public class ProgressNotification {

    private final int MAX_PROGRESS = 1000;
    private final int REQUEST_CODE = 0;

    public ProgressNotification(final MundraubBaseActivity activity, final int uniqueID,
                                     final ProgressableResult progress,
                                     int titleResourceId) {
        final NotificationCompat.Builder notification = new NotificationCompat.Builder(activity);
        notification.setAutoCancel(true);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker(activity.getText(R.string.app_name));
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle(activity.getText(titleResourceId));
        notification.setContentText(activity.getText(R.string.notification_download_in_progress));
        notification.setProgress(MAX_PROGRESS, 0, false);

        Intent intent = new Intent(activity, activity.getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        final NotificationManager notificationManager = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueID, notification.build());

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!progress.isDone()) {
                        sleep(1000);
                        double p = MAX_PROGRESS * progress.getProgress();
                        notification.setProgress(MAX_PROGRESS, (int)Math.round(p), false);
                        notificationManager.notify(uniqueID, notification.build());
                    }

                    notification.setContentText(activity.getText(R.string.notification_download_completed));
                    notification.setProgress(MAX_PROGRESS, MAX_PROGRESS, false);
                    notificationManager.notify(uniqueID, notification.build());
                } catch (InterruptedException e) {
                }
            }
        };
        thread.start();
    }
}
