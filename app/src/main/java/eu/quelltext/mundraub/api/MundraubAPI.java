package eu.quelltext.mundraub.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.quelltext.mundraub.Helper;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.plant.Plant;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class MundraubAPI extends API {

    public static final String HEADER_USER_AGENT = "Mundraub App (eu.quelltext.mundraub)";
    private final String URL_LOGIN = "https://mundraub.org/user/login";
    private final String URL_ADD_PLANT_FORM = "https://mundraub.org/node/add/plant/";
    private final int RETURN_CODE_LOGIN_SUCCESS = HttpURLConnection.HTTP_SEE_OTHER;
    private final int RETURN_CODE_LOGIN_FAILURE = HttpURLConnection.HTTP_OK;
    private List<HttpCookie> cookies = new ArrayList<HttpCookie>();

    public void authenticate(HttpURLConnection http) {
        // from https://stackoverflow.com/a/3249263
        for (HttpCookie cookie : cookies) {
            String s = cookie.getName() + "=" + cookie.getValue();
            http.addRequestProperty("Cookie", s);
            log.d("COOKIE", s);
        }
    }

    public void authenticate(Request.Builder builder) {
        // from https://stackoverflow.com/a/3249263
        for (HttpCookie cookie : cookies) {
            String s = cookie.getName() + "=" + cookie.getValue();
            builder.header("Cookie", s);
            //http.addRequestProperty("Cookie", );
            log.d("COOKIE", s);
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
        if (true) {
            Helper.trustAllConnections();
        }
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setInstanceFollowRedirects(false); // from https://stackoverflow.com/a/26046079/1320237
        return http;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        // from https://stackoverflow.com/a/25992879/1320237
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder
                    .followRedirects(false) // from https://stackoverflow.com/a/29268150/1320237
                    .followSslRedirects(false)
                    .build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int loginAsync(String username, String password) {
        try {
            // from https://stackoverflow.com/a/35013372/1320237
            URL url = new URL(URL_LOGIN);
            HttpsURLConnection http = (HttpsURLConnection) openSecureConnection(url);
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            byte[] loginData = getLoginData(username, password);
            http.setFixedLengthStreamingMode(loginData.length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.setRequestProperty("Referer", "https://mundraub.org/");
            http.setRequestProperty("Host", url.getHost());
            http.connect();
            OutputStream os = http.getOutputStream();
            os.write(loginData);
            os.flush();
            os.close();
            int returnCode = http.getResponseCode();
            if (returnCode == RETURN_CODE_LOGIN_FAILURE) {
                return R.string.invalid_credentials;
            } else if (returnCode != RETURN_CODE_LOGIN_SUCCESS) {
                log.e("LOGIN", "Unexpected return code " + returnCode + " when logging in.");
                try {
                    String result = Helper.getResultString(http);
                    log.d("LOGIN", result);
                } catch (IOException e) {
                    log.printStackTrace(e);
                }
                return R.string.error_unexpected_return_code;
            }
            String location = http.getHeaderField("Location"); // TODO: get user id
            String cookie = http.getHeaderField("Set-Cookie");
            setSessionFromCookie(cookie);
            return TASK_SUCCEEDED;
        } catch (UnknownHostException e) {
            log.printStackTrace(e);
            return R.string.error_unknown_hostname;
        } catch (MalformedURLException e) {
            log.printStackTrace(e);
        } catch (ProtocolException e) {
            log.printStackTrace(e);
        } catch (IOException e) {
            log.printStackTrace(e);
        } catch (NoSuchAlgorithmException e) {
            log.printStackTrace(e);
        } catch (KeyManagementException e) {
            log.printStackTrace(e);
            // TODO: new error message for different exceptions
        }
        return R.string.error_not_specified;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        if (plantExistsOnline(plantId)) {
            try {
                return deletePlantOnline(plantId);
            } catch (NoSuchAlgorithmException e) {
                log.printStackTrace(e);
            } catch (KeyManagementException e) {
                log.printStackTrace(e);
            } catch (IOException e) {
                log.printStackTrace(e);
                return R.string.error_connection;
            }
            return R.string.error_not_specified;

        } else {
            return TASK_SUCCEEDED;
        }
    }

    private int deletePlantOnline(String plantId) throws ErrorWithExplanation, NoSuchAlgorithmException, KeyManagementException, IOException {
        String plantDeleteUrl = "https://mundraub.org/node/" + plantId + "/delete";
        Map<String, String> formValues;
        try {
            formValues = getFormValues(plantDeleteUrl);
        } catch (IOException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified;
        } catch (KeyManagementException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified;
        } catch (NoSuchAlgorithmException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified;
        }
        return postFormTo(formValues, plantDeleteUrl, new FormPostHooks() {
            @Override
            public void buildForm(MultipartBody.Builder formBuilder) {
            }

            @Override
            public int responseSeeOther(String url) {
                return TASK_SUCCEEDED;
            }

            @Override
            public int responseOK() {
                return R.string.error_could_not_delete_plant;
            }

            @Override
            public int responseUnknown(int returnCode) {
                return R.string.error_unexpected_return_code;
            }
        });
    }

    private boolean plantExistsOnline(String plantId) throws ErrorWithExplanation {
        String plantUrl = "https://mundraub.org/node/" + plantId;
        Request request = new Request.Builder()
                .url(plantUrl)
                .build();
        OkHttpClient client = getUnsafeOkHttpClient();
        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.printStackTrace(e);
            abortOperation(R.string.error_count_not_check_plant);
            return false;
        }
        boolean result = response.code() == HttpURLConnection.HTTP_OK;
        log.d("PLANT EXISTS", plantUrl + " exists == " + result);
        return result;
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
            log.printStackTrace(e);
        } catch (ProtocolException e) {
            log.printStackTrace(e);
        } catch (IOException e) {
            log.printStackTrace(e);
        } catch (NoSuchAlgorithmException e) {
            log.printStackTrace(e);
        } catch (KeyManagementException e) {
            log.printStackTrace(e);
        } catch (JSONException e) {
            log.printStackTrace(e);
            return R.string.error_could_not_parse_open_street_map_data;
        }
        return R.string.error_not_specified;
    }

    private void fillInPlant(Map<String, String> formValues, Plant plant) throws IOException, ErrorWithExplanation, NoSuchAlgorithmException, KeyManagementException, JSONException {
        formValues.put("field_plant_category", plant.getCategory().getValueForAPI());
        formValues.put("field_plant_count_trees", plant.getFormCount());
        formValues.put("field_position[0][value]", plant.getPosition().forAPI());
        formValues.put("body[0][value]", plant.getDescription());
        //formValues.put("field_plant_image[0][_weight]", );
        //formValues.put("field_plant_image[0][display]", );
        String wholeAddress = getPlantAddressFromOpenStreetMap(plant);
        formValues.put("field_plant_address[0][value]", wholeAddress);
        // file attributes
        formValues.put("field_plant_image[0][_weight]", "0");
        formValues.put("field_plant_image[0][fids]", "");
        formValues.put("field_plant_image[0][display]", "1");

        // fields present but not in html
        formValues.put("address-search", getDisplayNameFromOSMJSONAddress(wholeAddress));
    }

    private String getDisplayNameFromOSMJSONAddress(String wholeAddress) throws JSONException {
        JSONObject json = new JSONObject(wholeAddress);
        return json.getString("display_name");
    }

    private String getPlantAddressFromOpenStreetMap(Plant plant) throws IOException, ErrorWithExplanation, KeyManagementException, NoSuchAlgorithmException {
        return getURL(plant.getPosition().getOpenStreetMapAddressUrl(), false);
    }

    private final String PATTERN_FORM_FIELD =
                    "<[^>]*(name|value)=\"([^\">]*)\"[^>]*(value|name)=\"([^\">]*)\"[^>]*>";

    private Map<String,String> getFormValues(String url) throws IOException, ErrorWithExplanation, KeyManagementException, NoSuchAlgorithmException {
        Map<String,String> result = new HashMap<String, String >();
        String document = getURL(url, true);
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
                log.d("getFormValues", "Could not parse " + fieldTag.toString());
                continue;
            }
            if (result.containsKey(name)) {
                log.d("getFormValues",
                        "Double key " + name + "=\"" + value + "\" ignored. " +
                        "It is assumed to come from a second form field.");
                continue;
            }
            result.put(name, value); // TODO: unescape html
            log.d("getFormValues", "Set " + name + "=\"" + value + "\"");
        }
        return result;
    }
    private String getURL(String url_, boolean authenticate) throws IOException, ErrorWithExplanation, NoSuchAlgorithmException, KeyManagementException {
        log.d("getURL", url_);
        URL url =  new URL(url_);
        HttpURLConnection http = (HttpURLConnection) openSecureConnection(url);
        http.setRequestMethod("GET");
        if (authenticate) {
            authenticate(http);
        }
        http.addRequestProperty("Host", url.getHost());
        http.addRequestProperty("User-Agent", HEADER_USER_AGENT);
        http.connect();
        try {
            int returnCode = http.getResponseCode();
            // from https://stackoverflow.com/a/1359700/1320237
            String result = Helper.getResultString(http);
            if (returnCode != HttpURLConnection.HTTP_OK) {
                log.d("getURL", result);
                log.d("getURL", "Unexpected return code " + returnCode);
                abortOperation(R.string.error_unexpected_return_code);
            }
            log.d("getURL", "Success " + url);
            return result;
        } finally {
            http.disconnect();
        }
    }

    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
    private static final MediaType MEDIA_TYPE_OCTET = MediaType.parse("application/octet-stream");

    private int postFormTo(Map<String, String> formValues, String url, FormPostHooks hooks) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        Helper.trustAllConnections();
        final OkHttpClient client = getUnsafeOkHttpClient();
        MultipartBody.Builder formBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        hooks.buildForm(formBuilder);
        for (String key : formValues.keySet()){
            formBuilder.addFormDataPart(key, formValues.get(key));
        }
        RequestBody requestBody = formBuilder.build();
        Buffer b = new Buffer();
        formBuilder.build().writeTo(b);
        b.copyTo(System.out);

        Request.Builder builder = new Request.Builder();
        authenticate(builder);
        Request request = builder
                .addHeader("Host", new URL(url).getHost())
                .addHeader("User-Agent", HEADER_USER_AGENT)
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Accept-Language", "en-US,en;q=0.5")
                .addHeader("Referer", "https://mundraub.org/map")
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        try{
            int returnCode = response.code();
            Headers headers = response.headers();
            for (String key: headers.names()) {
                log.d("RESPONSE HEADER", key + ": " + headers.get(key));
            }
            log.d("BODY", response.body().string());
            if (returnCode == HttpURLConnection.HTTP_OK) {
                log.d("postPlant", "Server rejected the data " + returnCode);
                return hooks.responseOK();
            } else if (returnCode != HttpURLConnection.HTTP_SEE_OTHER) {
                log.d("postPlant", "Unexpected return code " + returnCode);
                return hooks.responseUnknown(returnCode);
            }
            return hooks.responseSeeOther(response.header("Location"));
        } finally {
            response.close();
        }
    }

    interface FormPostHooks {
        void buildForm(MultipartBody.Builder formBuilder);
        int responseSeeOther(String url);
        int responseOK();
        int responseUnknown(int returnCode);
    }

    private int postPlantFormTo(Map<String, String> formValues, final Plant plant, String url) throws IOException, ErrorWithExplanation, KeyManagementException, NoSuchAlgorithmException, JSONException {
        fillInPlant(formValues, plant);
        return postFormTo(formValues, url, new FormPostHooks() {
            @Override
            public void buildForm(MultipartBody.Builder formBuilder) {
                if (plant.hasPicture()) {
                    File picture = plant.getPicture();
                    formBuilder.addFormDataPart("files[field_plant_image_0][]", picture.getName(),
                            RequestBody.create(MEDIA_TYPE_JPG, picture));
                } else {
                    formBuilder.addFormDataPart("files[field_plant_image_0][]", "",
                            RequestBody.create(MEDIA_TYPE_OCTET, "".getBytes()));
                }
            }
            @Override
            public int responseSeeOther(String seeOther) {
                try {
                    plant.online().publishedWithId(getPlantIdFromLocationUrl(seeOther));
                } catch (ErrorWithExplanation errorWithExplanation) {
                    return errorWithExplanation.getExplanationResourceId();
                }
                return TASK_SUCCEEDED;
            }
            @Override
            public int responseOK() {
                return R.string.error_could_not_post_plant;
            }
            @Override
            public int responseUnknown(int returnCode) {
                return R.string.error_unexpected_return_code;
            }
        });
    }

    private String getPlantIdFromLocationUrl(String location) throws ErrorWithExplanation {
        Matcher idMatch = Pattern.compile("nid=([0-9]+)").matcher(location);
        idMatch.find();
        String plantId = idMatch.group(1);
        if (plantId == null || plantId.isEmpty()) {
            abortOperation(R.string.error_no_plant_id);
        }
        return plantId;
    }

}
