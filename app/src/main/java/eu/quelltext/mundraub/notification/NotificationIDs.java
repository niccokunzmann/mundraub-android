package eu.quelltext.mundraub.notification;

public class NotificationIDs {
    /* publicly reserved ids */
    public static final int ID_MAP_DOWNLOAD = createNewId();
    public static final int ID_MARKER_DOWNLOAD = createNewId();

    private static int lastCreatedNotificationId = 0;
    public static int createNewId() {
        return ++lastCreatedNotificationId;
    }
}
