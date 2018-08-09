package eu.quelltext.mundraub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Locale;

public final class Helper {
    public static void deleteDir(File file) {
        // from https://stackoverflow.com/a/29175213/1320237
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

    public static String getResultString(HttpURLConnection http) throws IOException {
        InputStream is = http.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        return response.toString();
    }

    public static boolean saveUrlToFile(String url, File file) throws IOException {
        // from https://stackoverflow.com/a/921400/1320237
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        long l = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return l != 0;
    }

    public static String doubleTo15DigitString(double d) {
        // from https://stackoverflow.com/a/8820013/1320237
        // from https://stackoverflow.com/questions/4885254/string-format-to-format-double-in-java#comment5434509_4885329
        return String.format(Locale.US, "%.15f", d);
    }
}
