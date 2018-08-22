package eu.quelltext.mundraub.error;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Scanner;

/*
    Copy STDOUT and STDERR to a file
 */
public class Logger implements UncaughtExceptionHandler {

    private static final int TAG_MAX_LEGTH = 23; // from https://stackoverflow.com/a/28168739/1320237
    private static final String TAG_DIVIDER = ": ";
    private static Logger logger; // initialize as soon as possible;
    private static final String LOG_FILE_NAME = "eu.quelltext.mundraub.log.txt";
    private final UncaughtExceptionHandler defaultExceptionHandler;
    private final PrintStream logStream;
    private final String TAG = "LOGGER" + TAG_DIVIDER;

    static {
        // crate logger as soon as possible
        getInstance();
    }

    public static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    private Logger() {
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        PrintStream log1;
        try {
            log1 = new PrintStream(new FileOutputStream(getLogFile(), false));
        } catch (FileNotFoundException e) {
            this.printStackTrace(TAG, e);
            log1 = null;
        }
        logStream = log1;
        i(TAG, "-------------- App started --------------");
    }

    private File getLogFile() {
        // from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
        String root = Environment.getExternalStorageDirectory().toString();
        return new File(root, LOG_FILE_NAME);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (logStream != null) {
            printStackTrace(TAG, e);
        }
        defaultExceptionHandler.uncaughtException(t, e);
    }

    public static Log newFor(Loggable loggable) {
        return new Log(getInstance(), loggable.getTag());
    }

    public static Log newFor(String tag) {
        return new Log(getInstance(), tag);
    }

    public static Log newFor(Object o) {
        return new Log(getInstance(), o.getClass().getName());
    }

    public interface Loggable {
        String getTag();
    }

    public static class Log {

        private final String tag;

        private Log(Logger logger, String tag) {
            this.tag = tag.substring(0, tag.length() < TAG_MAX_LEGTH? tag.length() : TAG_MAX_LEGTH);
        }

        public void d(String tag, String s) {
            logger.d(this.tag, tag + TAG_DIVIDER + s);
        }

        public void e(String tag, String s) {
            logger.e(this.tag, tag + TAG_DIVIDER + s);
        }

        public void i(String tag, String s) {
            logger.i(this.tag, tag + TAG_DIVIDER + s);
        }

        public void printStackTrace(Exception e) {
            logger.printStackTrace(tag, e);
        }

        public void secure(String tag, String secret) {
            logger.secure(this.tag, tag, secret);
        }
    }

    private void printStackTrace(String tag, Throwable e) {
        e.printStackTrace();
        if (logStream != null) {
            logStream.print(tag);
            e.printStackTrace(logStream);
            logStream.flush();
        }
    }

    private void d(String tag, String s) {
        android.util.Log.d(tag, s);
        print("DEGUG" + TAG_DIVIDER + tag, s);
    }

    private void e(String tag, String s) {
        android.util.Log.d(tag, s);
        print("ERROR" + TAG_DIVIDER + tag, s);
    }

    private void i(String tag, String s) {
        android.util.Log.i(tag, s);
        print("INFO" + TAG_DIVIDER + tag, s);
    }

    private void secure(String tag1, String tag2, String secret) {
        android.util.Log.i(tag1, tag2 + TAG_DIVIDER + secret);
        print("SECURE" + TAG_DIVIDER + tag1 + TAG_DIVIDER + tag2 + TAG_DIVIDER, fillString(secret.length(), "*"));
    }


    private void print(String tag, String s) {
        if ( logStream == null) {
            return;
        }
        Scanner scanner = new Scanner(s);
        tag = tag + ": ";
        String spaces = fillString(tag.length(), " ");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            logStream.print(tag);
            logStream.print(line);
            tag = spaces;
        }
        scanner.close();
        logStream.flush();
    }

    private String fillString(int length, String character) {
        return new String(new char[length]).replace("\0", character); // from https://stackoverflow.com/a/16812721/1320237;
    }
}
