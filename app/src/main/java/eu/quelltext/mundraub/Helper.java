package eu.quelltext.mundraub;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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

    public static boolean saveUrlToFile(String url_, File file) throws IOException {
        FileUtils.copyURLToFile(new URL(url_), file);
        return true;
        /*
        // from https://stackoverflow.com/a/921400/1320237
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        long l = fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return l != 0;*/
        /*
        Log.d("saveUrlToFile", url_);
        URL url =  new URL(url_);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.addRequestProperty("Host", url.getHost());
        http.addRequestProperty("User-Agent", MundraubAPI.HEADER_USER_AGENT);
        http.connect();
        try {
            int returnCode = http.getResponseCode();
            if (returnCode != HttpURLConnection.HTTP_OK) {
                Log.d("saveUrlToFile", "Unexpected return code " + returnCode);
                return false;
            }
            FileOutputStream toFile = new FileOutputStream(file, false);
            BufferedReader fromFile = new BufferedReader(new InputStreamReader(http.getInputStream()));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fromFile.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            toFile.close();
            fromFile.close();
            Log.d("saveUrlToFile", "Success " + url + "\n to " + file.toString());
            return true;
        } finally {
            http.disconnect();
        }*/
    }

    public static String doubleTo15DigitString(double d) {
        // from https://stackoverflow.com/a/8820013/1320237
        // from https://stackoverflow.com/questions/4885254/string-format-to-format-double-in-java#comment5434509_4885329
        return String.format(Locale.US, "%.15f", d);
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
}
