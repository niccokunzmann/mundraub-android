package eu.quelltext.mundraub.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.ErrorAware;
import eu.quelltext.mundraub.map.position.IPosition;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public final class Helper extends ErrorAware {
    private static final double EARTH_RADIUS_METERS = 6399594.;

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

    public static long folderSize(File directory) {
        // from https://stackoverflow.com/a/2149807
        long length = 0;
        File[] content = directory.listFiles();
        if (content != null) {
            for (File file : content) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        }
        return length;
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
        OkHttpClient client = Settings.getOkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Referer", "http://app.mundraub.quelltext.eu/")
                .build();
        Response response = client.newCall(request).execute();
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(response.body().source());
        sink.close();
        // this is elegant but we need a referer header
        // FileUtils.copyURLToFile(new URL(url_), file); // from https://stackoverflow.com/a/7156178
        return true;
    }

    public static String doubleTo15DigitString(double d) {
        // from https://stackoverflow.com/a/8820013/1320237
        // from https://stackoverflow.com/questions/4885254/string-format-to-format-double-in-java#comment5434509_4885329
        return String.format(Locale.US, "%.15f", d);
    }

    public static AlertDialog.Builder getAlertBuilder(Context context) {
        // from https://stackoverflow.com/a/2115770/1320237
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            return new AlertDialog.Builder(context);
        }
    }

    public static boolean setBitmapFromFileOrNull(File file, ImageView imageView) {
        if (file == null || !file.exists()) return false;
        Uri uri = Uri.fromFile(file);
        Context context = imageView.getContext();
        Bitmap bitmap;
        try {
            // from https://stackoverflow.com/a/31930502/1320237
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            return false;
        }
        if (bitmap == null) {
            return false;
        }
        imageView.setImageBitmap(bitmap);
        return true;
    }

    public static double metersToDegrees(double distanceInMeters) {
        return distanceInMeters / EARTH_RADIUS_METERS / DEG_TO_RAD;
    }

    private static int[] DIRECTIONS = new int[]{
            R.string.direction_north, R.string.direction_north_east, R.string.direction_east,
            R.string.direction_south_east, R.string.direction_south, R.string.direction_south_west,
            R.string.direction_west, R.string.direction_north_west
    };

    public static int directionFromPositionToPositionAsResourceId(IPosition me, IPosition other) {
        return directionFromPositionToPositionAsResourceId(
                me.getLongitude(), me.getLatitude(),
                other.getLongitude(), other.getLatitude());
    }

            /*
     * Return the direction from one position to another.
     * This assumes that both are very close to each other and thus form an euclidian planar.
     */
    public static int directionFromPositionToPositionAsResourceId(
            double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
        if (fromLatitude == toLatitude && fromLongitude == toLongitude) {
            return R.string.direction_too_close;
        }
        double alpha = Math.atan2(toLongitude - fromLongitude, toLatitude - fromLatitude) / Math.PI / 2 * DIRECTIONS.length;
        int i = (int)Math.floor(alpha + 0.5 + DIRECTIONS.length) % DIRECTIONS.length;
        return DIRECTIONS[i];
    }

    private static double DEG_TO_RAD = Math.PI / 180;
    private static double deg2rad(double degrees) {
        return degrees * DEG_TO_RAD;
    }

    public static double distanceInMetersBetween(double longitude1, double latitude1, double longitude2, double latitude2) {
        // convert from degree to radial
        double phi1 = deg2rad(latitude1);
        double phi2 = deg2rad(latitude2);
        double lambda1 = deg2rad(longitude1);
        double lambda2 = deg2rad(longitude2);
        // compute the deltas
        double dPhi = phi1 - phi2; // Δφ
        double dLambda = lambda1 - lambda2; // Δλ
        // compute haversine formula
        double dRoh = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(dPhi / 2), 2) +
                        Math.cos(phi1) * Math.cos(phi2) *
                                Math.pow(Math.sin(dLambda / 2), 2)
        ));
        double distance = EARTH_RADIUS_METERS * dRoh;
        return distance;
    }

    private static class TrustAllX509TrustManager implements X509TrustManager {
        // from https://stackoverflow.com/a/19723687/1320237
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) {
        }
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                       String authType) {
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static void trustAllConnections() throws KeyManagementException, NoSuchAlgorithmException {
        // trust all certificates, see
        // https://github.com/niccokunzmann/mundraub-android/issues/3
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        });
    }

    public static int compare(long a , long b) {
        if (a < b) return -1;
        if (a > b) return 1;
        return 0;
    }

    public static Map<String, List<String>> splitQuery(String query) throws UnsupportedEncodingException {
        // splitQuery from https://stackoverflow.com/a/13592567
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }
}
