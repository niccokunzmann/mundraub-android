package eu.quelltext.mundraub.initialization;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Initialization {

    private static final List<ContextInitialized> initializers = new ArrayList<ContextInitialized>();
    private static Context context = null;

    public interface ContextInitialized {
        void setContext(Context context);
    }

    public static void provideContextFor(ContextInitialized callback) {
        if (hasContext()) {
            callback.setContext(context);
        } else {
            initializers.add(callback);
        }
    }

    public static void provideContext(Context newContext) {
        if (!hasContext()) {
            context = newContext;
            for (ContextInitialized callback : initializers) {
                callback.setContext(context);
            }
            initializers.clear();
        }
    }

    public static boolean hasContext() {
        return context != null;
    }

}
