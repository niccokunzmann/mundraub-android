package eu.quelltext.mundraub.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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

import eu.quelltext.mundraub.error.ErrorAware;

public final class Helper extends ErrorAware {
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

    public static boolean saveUrlToFile(String url_, File file) throws IOException {
        FileUtils.copyURLToFile(new URL(url_), file); // from https://stackoverflow.com/a/7156178
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
