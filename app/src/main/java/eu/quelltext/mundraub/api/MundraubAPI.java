package eu.quelltext.mundraub.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.PlantsCache;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;
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
    private final String URL_SIGNUP = "https://mundraub.org/user/register";
    private final String URL_ADD_PLANT_FORM = "https://mundraub.org/node/add/plant/";
    private final int RETURN_CODE_LOGIN_SUCCESS = HttpURLConnection.HTTP_SEE_OTHER;
    private final int RETURN_CODE_LOGIN_FAILURE = HttpURLConnection.HTTP_OK;
    private List<HttpCookie> cookies = new ArrayList<HttpCookie>();

    public void authenticate(HttpURLConnection http) {
        // from https://stackoverflow.com/a/3249263
        for (HttpCookie cookie : cookies) {
            String s = cookie.getName() + "=" + cookie.getValue();
            http.addRequestProperty("Cookie", s);
            log.secure("COOKIE", s);
        }
    }

    public void authenticate(Request.Builder builder) {
        // from https://stackoverflow.com/a/3249263
        for (HttpCookie cookie : cookies) {
            String s = cookie.getName() + "=" + cookie.getValue();
            builder.header("Cookie", s);
            //http.addRequestProperty("Cookie", );
            log.secure("COOKIE", s);
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
        if (Settings.useInsecureConnections()) {
            Helper.trustAllConnections();
        }
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setInstanceFollowRedirects(false); // from https://stackoverflow.com/a/26046079/1320237
        return http;
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
        } catch (Exception e) {
            return handleExceptionConsistently(e);
        }
    }

    @Override
    protected int signupAsync(String email, String username, String password) throws ErrorWithExplanation {
        try {
            Map<String, String> formValues = getFormValues(URL_SIGNUP);
            formValues.put("name", username);
            formValues.put("mail", email);
            formValues.put("pass[pass1]", password);
            formValues.put("pass[pass2]", password);
            //formValues.put("op", formValues.get("op").replace(" ", "+"));
            logFormValues("signupAsync", formValues);
            Thread.sleep(20000); // the signup process uses a honey pod for bots https://www.drupal.org/project/honeypot
            return postFormTo(formValues, URL_SIGNUP, new FormPostHooks() {
                @Override
                public void buildForm(MultipartBody.Builder formBuilder) {
                }

                @Override
                public int responseSeeOther(String url) {
                    return TASK_SUCCEEDED;
                }

                @Override
                public int responseOK() {
                    return R.string.error_could_not_sign_up_mundraub;
                }

                @Override
                public int responseUnknown(int returnCode) {
                    return R.string.error_unexpected_return_code;
                }
            });
        } catch (Exception e) {
            return handleExceptionConsistently(e);
        }
    }

    private void logFormValues(String tag, Map<String, String> formValues) {
        for (String key : formValues.keySet()) {
            log.d(tag, key + "=" + formValues.get(key));
        }
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        if (plantExistsOnline(plantId)) {
            try {
                return deletePlantOnline(plantId);
            } catch (Exception e) {
                return handleExceptionConsistently(e);
            }
        } else {
            return TASK_SUCCEEDED;
        }
    }

    private int deletePlantOnline(String plantId) throws ErrorWithExplanation, NoSuchAlgorithmException, KeyManagementException, IOException {
        String plantDeleteUrl = "https://mundraub.org/node/" + plantId + "/delete";
        Map<String, String> formValues;
        try {
            formValues = getFormValues(plantDeleteUrl);
        } catch (Exception e) {
            return handleExceptionConsistently(e);
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
        OkHttpClient client = Settings.getOkHttpClient();
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
    protected Set<String> getUrlsForAllPlants() {
        HashSet<String> urls = new HashSet<String>();
        for (PlantCategory category: PlantCategory.all()) {
            if (category.canBeUsedByAPI(this)) {
                // see https://github.com/niccokunzmann/mundraub-android/issues/96
                urls.add("https://mundraub.org/cluster/plant?bbox=-180.0,-90.0,0.0,0&zoom=18&cat=" + category.getFieldFor(this));
                urls.add("https://mundraub.org/cluster/plant?bbox=0.0,-90.0,180.0,0&zoom=18&cat=" + category.getFieldFor(this));
                urls.add("https://mundraub.org/cluster/plant?bbox=-180.0,0.0,0.0,90&zoom=18&cat=" + category.getFieldFor(this));
                urls.add("https://mundraub.org/cluster/plant?bbox=0.0,0.0,180.0,90&zoom=18&cat=" + category.getFieldFor(this));
            }
        }
        return urls;
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {
        if (data.length() > 5) { // data is null
            JSONObject json = new JSONObject(data);
            PlantsCache.updateMundraubPlantMarkers(json, fraction);
        }
    }

    @Override
    public String getPlantUrl(String id) {
        return "https://mundraub.org/map?nid=" + id;
    }

    @Override
    public int nameResourceId() {
        return R.string.login_api_name_mundraub;
    }

    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        try {
            Map<String, String> formValues = getFormValues(URL_ADD_PLANT_FORM);
            return postPlantFormTo(formValues, plant, URL_ADD_PLANT_FORM);
        } catch (JSONException e) {
            log.printStackTrace(e);
            return R.string.error_could_not_parse_open_street_map_data;
        } catch (Exception e) {
            return handleExceptionConsistently(e);
        }
    }

    private void fillInPlant(Map<String, String> formValues, Plant plant) throws IOException, ErrorWithExplanation, NoSuchAlgorithmException, KeyManagementException, JSONException {
        formValues.put("field_plant_category", plant.getCategory().getFieldFor(this));
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
        return httpGet(plant.getPosition().getOpenStreetMapAddressUrl());
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
        Helper.trustAllConnections(); // TODO: do we need this?
        final OkHttpClient client = Settings.getOkHttpClient();
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
                if (key.toLowerCase().equals("set-cookie")) {
                    log.secure("RESPONSE HEADER", key + ": " + headers.get(key));
                } else {
                    log.d("RESPONSE HEADER", key + ": " + headers.get(key));
                }
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
                    plant.online().publishedWithId(getPlantIdFromLocationUrl(seeOther), MundraubAPI.this);
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

    @Override
    public String id() {
        return Settings.API_ID_MUNDRAUB;
    }

    public int radioButtonId(){
        return R.id.radioButton_mundraub;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }
}
