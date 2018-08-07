package eu.quelltext.mundraub.api;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.plant.Plant;

public class MundraubAPI extends API {

    private final String URL_LOGIN = "https://mundraub.org/user/login";
    private final String URL_ADD_PLANT_FORM = "https://mundraub.org/node/add/plant/";
    private final int RETURN_CODE_LOGIN_SUCCESS = HttpURLConnection.HTTP_SEE_OTHER;
    private final int RETURN_CODE_LOGIN_FAILURE = HttpURLConnection.HTTP_OK;
    private List<HttpCookie> cookies = new ArrayList<HttpCookie>();

    private class TrustAllX509TrustManager implements X509TrustManager {
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

    private byte[] getLoginData(String username, String password) throws UnsupportedEncodingException {
        // from https://stackoverflow.com/a/35013372/1320237
        String data =
                "name=" + URLEncoder.encode(username, "UTF-8") +
                "&pass=" + URLEncoder.encode(password, "UTF-8") +
                "&form_build_id=form-ZuIG3EC3U6r_8Kgflk36X1Eq0u8RPhOsltB-ZEVRaws" +
                "&form_id=user_login_form&op=Anmelden";
        // from https://stackoverflow.com/a/5729823/1320237
        return data.getBytes("UTF-8");
    }

    private void setSessionFromCookie(String header) {
        this.cookies = HttpCookie.parse(header);
    }

    private URLConnection openSecureConnection(URL url) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
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
        return url.openConnection();
    }

    @Override
    protected int loginAsync(String username, String password) {
        try {
            // from https://stackoverflow.com/a/35013372/1320237
            HttpsURLConnection http = (HttpsURLConnection)openSecureConnection(new URL(URL_LOGIN));
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            byte[] loginData = getLoginData(username, password);
            http.setFixedLengthStreamingMode(loginData.length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.setRequestProperty("Referer", "https://mundraub.org/");
            http.connect();
            OutputStream os = http.getOutputStream();
            os.write(loginData);
            os.flush();
            os.close();
            int returnCode = http.getResponseCode();
            if (returnCode == RETURN_CODE_LOGIN_FAILURE) {
                return R.string.invalid_credentials;
            } else if (returnCode != RETURN_CODE_LOGIN_SUCCESS) {
                Log.e("DEBUG", "Unexpected return code " + returnCode + " when logging in.");
                return R.string.error_unexpected_return_code;
            }
            String location = http.getHeaderField("Location"); // TODO: get user id
            String cookie = http.getHeaderField("Set-Cookie");
            setSessionFromCookie(cookie);
            return TASK_SUCCEEDED;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
            // TODO: new error message for different exceptions
        }
        return R.string.error_not_specified;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        try {
            Map<String, String> formValues = getFormValues(URL_ADD_PLANT_FORM);
            postPlantFormTo(formValues, plant, URL_ADD_PLANT_FORM);
            return R.string.error_not_implemented; // TODO: TASK_SUCCEEDED
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.string.error_not_specified;
    }

    private Map<String,String> getFormValues(String sUrl) throws IOException, ErrorWithExplanation {
        Map<String,String> result = new HashMap<String, String >();
        URL url = new URL(sUrl);
        HttpsURLConnection http = (HttpsURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.connect();
        http.setDoOutput(false);
        int returnCode = http.getResponseCode();
        if (returnCode != HttpURLConnection.HTTP_OK) {
            abortOperation(R.string.error_unexpected_return_code);
        }
        // TODO: implement
        return result;
    }

    private void postPlantFormTo(Map<String, String> defaultValues, Plant plant, String url_add_plant_form) {
        // TODO: implement
    }
}
