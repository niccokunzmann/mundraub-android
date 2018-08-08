package eu.quelltext.mundraub.api;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void authenticate(HttpsURLConnection http) {
        // from https://stackoverflow.com/a/3249263
        for (HttpCookie cookie : cookies) {
            http.addRequestProperty("Cookie", cookie.getName() + "=" + cookie.getValue());
            Log.d("COOKIE", cookie.getName() + "=" + cookie.getValue());
        }
    }

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
            return postPlantFormTo(formValues, plant, URL_ADD_PLANT_FORM);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.string.error_not_specified;
    }

    private void fillInPlant(Map<String, String> formValues, Plant plant) throws IOException, ErrorWithExplanation {
        formValues.put("field_plant_category", plant.getCategory().getId());
        formValues.put("field_plant_count_trees", plant.getFormCount());
        formValues.put("field_position[0][value]", "POINT(" + plant.getLongitude() + " " + plant.getLatitude() + ")");
        formValues.put("body[0][value]", plant.getDescription());
        //formValues.put("field_plant_image[0][_weight]", );
        //formValues.put("field_plant_image[0][display]", );
        formValues.put("field_plant_address[0][value]", getPlantAddressFromOpenStreetMap(plant));
        // file attributes
        formValues.put("field_plant_image[0][_weight]", "0");
        formValues.put("field_plant_image[0][fids]", "");
        formValues.put("field_plant_image[0][display]", "1");

    }

    private String getPlantAddressFromOpenStreetMap(Plant plant) throws IOException, ErrorWithExplanation {
        // examples:
        // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469471
        // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469470
        // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469469
        // https://nominatim.openstreetmap.org/reverse?callback=nominatimGeocodeCallback&json_callback=nominatimGeocodeCallback&zoom=18&lon=13.096788167604247&lat=52.38594659593905&format=json&_=1533731469468
        return getURL("https://nominatim.openstreetmap.org/reverse?zoom=18&lon=" +
                       plant.getLongitude() + "&lat=" + plant.getLatitude() + "&format=json");
    }

    private final String PATTERN_FORM_FIELD =
                    "<[^>]*(name|value)=\"([^\">]*)\"[^>]*(value|name)=\"([^\">]*)\"[^>]*>";

    private Map<String,String> getFormValues(String url) throws IOException, ErrorWithExplanation {
        Map<String,String> result = new HashMap<String, String >();
        String document = getURL(url);
        // from https://stackoverflow.com/a/32788546/1320237
        Matcher fieldTag = Pattern.compile(PATTERN_FORM_FIELD).matcher(document);
        while (fieldTag.find()) {
            String attributeName1 = fieldTag.group(1);
            String attributeValue1 = fieldTag.group(2);
            String attributeName2 = fieldTag.group(3);
            String attributeValue2 = fieldTag.group(4);
            //Log.d("attribute 1", attributeName1 + "=" + attributeValue1);
            //Log.d("attribute 2", attributeName2 + "=" + attributeValue2);
            String name = null;
            String value = null;
            if (attributeName1.equals("name") && attributeName2.equals("value")) {
                name = attributeValue1;
                value = attributeValue2;
            } else if (attributeName2.equals("name") && attributeName1.equals("value")) {
                name = attributeValue2;
                value = attributeValue1;
            } else {
                Log.d("getFormValues", "Could not parse " + fieldTag.toString());
                continue;
            }
            if (result.containsKey(name)) {
                Log.d("getFormValues",
                        "Double key " + name + "=\"" + value + "\" ignored. " +
                        "It is assumed to come from a second form field.");
                continue;
            }
            result.put(name, value); // TODO: unescape html
            Log.d("getFormValues", "Set " + name + "=\"" + value + "\"");
        }
        return result;
    }
    private String getURL(String sUrl) throws IOException, ErrorWithExplanation {
        URL url = new URL(sUrl);
        HttpsURLConnection http = (HttpsURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        authenticate(http);
        http.connect();
        try {
            int returnCode = http.getResponseCode();
            if (returnCode != HttpURLConnection.HTTP_OK) {
                abortOperation(R.string.error_unexpected_return_code);
            }
            // from https://stackoverflow.com/a/1359700/1320237
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
        } finally {
            http.disconnect();
        }
    }

    private int postPlantFormTo(Map<String, String> formValues, Plant plant, String url) throws IOException, ErrorWithExplanation {
        fillInPlant(formValues, plant);
        // from https://stackoverflow.com/a/35013372
        HttpsURLConnection http = (HttpsURLConnection) new URL(url).openConnection();
        http.setRequestMethod("POST");
        authenticate(http);
        String boundary = UUID.randomUUID().toString();
        byte[] boundaryBytes =
                ("--" + boundary + "\r\n").getBytes("UTF-8");
        byte[] finishBoundaryBytes =
                ("--" + boundary + "--").getBytes("UTF-8");
        http.setRequestProperty("Content-Type",
                "multipart/form-data; charset=UTF-8; boundary=" + boundary);

        // Enable streaming mode with default settings
        http.setChunkedStreamingMode(0);

        // Send our fields:
        OutputStream out = http.getOutputStream();
        try {
            for (String key : formValues.keySet()){
                sendField(out, key, formValues.get(key));
                out.write(boundaryBytes);
            }
            // Send our file
            if (plant.hasPicture()) {
                InputStream file = new FileInputStream(plant.getPicture());
                try {
                    sendFile(out, "files[field_plant_image_0][]", file, plant.getPicture().getName());

                } finally {
                    file.close();
                }
            } else {
                // from http://stackoverflow.com/questions/5720524/ddg#5720542
                InputStream empty = new ByteArrayInputStream( "".getBytes() );
                sendFile(out, "files[field_plant_image_0][]", empty, "");
            }
            // Finish the request
            out.write(finishBoundaryBytes);
        } finally {
            out.close();
        }
        int returnCode = http.getResponseCode();
        if (returnCode == HttpURLConnection.HTTP_OK) {
            return R.string.error_could_not_post_plant;
        } else if (returnCode != HttpURLConnection.HTTP_SEE_OTHER) {
            return R.string.error_unexpected_return_code;
        }
        String location = http.getHeaderField("Location"); // TODO: get user id
        // example location = "https://mundraub.org/map?nid=77627"
        Matcher idMatch = Pattern.compile("nid=([0-9]+)").matcher(location);
        idMatch.find();
        String plantId = idMatch.group(1);
        if (plant == null || plantId.isEmpty()) {
            return R.string.error_no_plant_id;
        }
        plant.online().publishedWithId(plantId);
        return TASK_SUCCEEDED;
    }

    /*  -----------------------------77394568618088815351035398667
        Content-Disposition: form-data; name="files[field_plant_image_0][]"; filename=""
        Content-Type: application/octet-stream
        -----------------------------77394568618088815351035398667
        Content-Disposition: form-data; name="field_plant_image[0][_weight]"
        0
        -----------------------------77394568618088815351035398667
        Content-Disposition: form-data; name="field_plant_image[0][fids]"
        -----------------------------77394568618088815351035398667
        Content-Disposition: form-data; name="field_plant_image[0][display]"
        1
    */
    private void sendFile(OutputStream out, String name, InputStream in, String fileName) throws IOException {
        // from https://stackoverflow.com/a/35013372
        String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name,"UTF-8")
                + "\"; filename=\"" + URLEncoder.encode(fileName,"UTF-8") + "\"" +
                "\r\nContent-Type: " + (fileName.isEmpty() ? "application/octet-stream" : "image/jpeg") + "\r\n\r\n";
        out.write(o.getBytes("UTF-8"));
        byte[] buffer = new byte[2048];
        for (int n = 0; n >= 0; n = in.read(buffer))
            out.write(buffer, 0, n);
        out.write("\r\n".getBytes("UTF-8"));
    }

    private void sendField(OutputStream out, String name, String field) throws IOException {
        // from https://stackoverflow.com/a/35013372
        String o = "Content-Disposition: form-data; name=\""
                + URLEncoder.encode(name,"UTF-8") + "\"\r\n\r\n";
        out.write(o.getBytes("UTF-8"));
        out.write(URLEncoder.encode(field,"UTF-8").getBytes("UTF-8"));
        out.write("\r\n".getBytes("UTF-8"));
    }
}
