package eu.quelltext.mundraub.initialization;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.map.TilesCache;

public class Initialization {

    private static final List<ActivityInitialized> initializers = new ArrayList<ActivityInitialized>();
    private static Activity activity = null;

    static {
        initializers.add(new ActivityInitialized() {
            @Override
            public void setActivity(Activity context) {
                FruitRadarNotification.initialize();
                TilesCache.initializeCachesOnDirectory(context.getCacheDir());
            }
        });
    }

    public static Activity getActivity() {
        return activity;
    }

    public interface ActivityInitialized {
        void setActivity(Activity context);
    }

    public static void provideActivityFor(ActivityInitialized callback) {
        if (hasActivity()) {
            callback.setActivity(activity);
        } else {
            initializers.add(callback);
        }
    }

    public static void provideActivity(Activity newActivity) {
        if (!hasActivity()) {
            activity = newActivity;
            for (ActivityInitialized callback : initializers) {
                callback.setActivity(activity);
            }
            initializers.clear();
        }
    }

    public static boolean hasActivity() {
        return activity != null;
    }

}
