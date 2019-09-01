package eu.quelltext.mundraub.api;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.PlantsCache;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

/*
 * Implement the na-ovoce API.
 * See https://github.com/jsmesami/naovoce/blob/master/API.apib
 */
public class NaOvoceAPI extends API {

    private static final String URL_SIGNUP = "/api/v1/signup/";
    private static final String URL_LOGIN = "/api/v1/token/";
    private static final String URL_ADD_PLANT = "/api/v1/fruit/";
    private static final String URL_UPDATE_PLANT = "/api/v1/fruit/";
    private static final String URL_DELETE_PLANT = "/api/v1/fruit/";
    private static final String JSON_TOKEN = "token";
    private static final String JSON_ID = "id";
    private static final String JSON_DETAIL = "detail";
    private static final String JSON_NON_FIELD_ERRORS = "non_field_errors";
    private static final String JSON_NON_FIELD_ERRORS_NO_LOGIN = "Zadanými údaji se nebylo možné přihlásit.";


    private String token = null;

    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        return sendPlantTo(plant, "POST", URL_ADD_PLANT);
    }

    private int sendPlantTo(Plant plant, String method, String url) throws ErrorWithExplanation {
        JSONObject request = new JSONObject();
        try {
            // https://github.com/jsmesami/naovoce/blob/master/API.apib#L214
            request.put("lat", positionString(plant.getLatitude()));
            request.put("lng", positionString(plant.getLongitude()));
            request.put("kind", plant.getCategory().getFieldFor(this));
            request.put("description", plant.getDescription());
        } catch (JSONException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified; // unlikely
        }
        try {
            JSONObject response = sendJSONTo(request, method, host() + url);
            // check the response
            // https://github.com/jsmesami/naovoce/blob/master/API.apib#L48
            if (!response.has(JSON_ID)) {
                return R.string.error_could_not_post_plant;
            }
            plant.online().publishedWithId(Integer.toString(response.getInt(JSON_ID)), this);
        } catch (IOException e) {
            return handleExceptionConsistently(e);
        } catch (JSONException e) {
            return handleExceptionConsistently(e);
        }
        return TASK_SUCCEEDED;
    }

    private String positionString(double degrees) {
        // Zkontrolujte, že číslo neobsahuje více než 13 čislic.
        // https://github.com/jsmesami/naovoce/blob/master/src/fruit/models.py#L77
        String result = Double.toString(degrees);
        if (result.length() > 13) {
            return result.substring(0, 13);
        }
        return result;
    }

    @Override
    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
        JSONObject request = new JSONObject();
        try {
            request.put("username", username);
            request.put("password", password);
        } catch (JSONException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified; // unlikely
        }
        try {
            JSONObject response = postJSONTo(request, host() + URL_LOGIN);
            // check the response
            // https://github.com/jsmesami/naovoce/blob/master/API.apib#L48
            if (!response.has(JSON_TOKEN)) {
                return R.string.error_could_not_log_in_na_ovoce;
            }
            this.token = response.getString(JSON_TOKEN);
        } catch (IOException e) {
            return handleExceptionConsistently(e);
        } catch (JSONException e) {
            return handleExceptionConsistently(e);
        }
        return TASK_SUCCEEDED;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        // https://github.com/jsmesami/naovoce/blob/master/API.apib#L311
        JSONObject reason = new JSONObject();
        try {
            reason.put("why_deleted", "The user removed the plant with the app. I do not know why.");
        } catch (JSONException e) {
            return R.string.error_not_specified;
        }
        try {
            sendJSONTo(reason, "DELETE", host() + URL_DELETE_PLANT + plantId + "/");
            return TASK_SUCCEEDED;
        } catch (IOException e) {
            return handleExceptionConsistently(e);
        } catch (JSONException e) {
            return handleExceptionConsistently(e);
        }
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        // https://github.com/jsmesami/naovoce/blob/master/API.apib#L277
        return sendPlantTo(plant, "PATCH", URL_UPDATE_PLANT + plantId + "/");
    }

    @Override
    protected int signupAsync(String email, String username, String password) throws ErrorWithExplanation {
        JSONObject request = new JSONObject();
        try {
            request.put("email", email);
            request.put("username", username);
            request.put("password", password);
        } catch (JSONException e) {
            log.printStackTrace(e);
            return R.string.error_not_specified; // unlikely
        }
        try {
            JSONObject response = postJSONTo(request, host() + URL_SIGNUP);
            // check the response
            // https://github.com/jsmesami/naovoce/blob/master/API.apib#L48
            if (!response.has(JSON_ID)) {
                return R.string.error_could_not_sign_up_na_ovoce;
            }
        } catch (IOException e) {
            return handleExceptionConsistently(e);
        } catch (JSONException e) {
            return handleExceptionConsistently(e);
        }
        return TASK_SUCCEEDED;
    }

    private JSONObject postJSONTo(final JSONObject content, String url) throws IOException, ErrorWithExplanation, JSONException {
        return sendJSONTo(content, "POST", url);
    }

    private JSONObject sendJSONTo(final JSONObject content, String method, String url) throws IOException, ErrorWithExplanation, JSONException {
        OkHttpClient client = Settings.getOkHttpClient();
        log.d("postJSONTo", url);
        final String contentString = content.toString(2);
        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.get("application/json");
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8(contentString);
            }
        };
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .method(method, requestBody);
        if (token != null) {
            // https://github.com/jsmesami/naovoce/blob/master/API.apib#L232
            builder.addHeader("Authorization", "Token " + token);
        }
        Request request = builder.build();
        okhttp3.Response response = client.newCall(request).execute();
        int code = response.code();
        String responseBody = response.body().string();
        log.d("response", responseBody);
        if (code == 204 /*DELETE*/) {
            return new JSONObject();
        }
        if (code >= 300 && code < 400) {
            abortOperation(R.string.error_redirect_no_api_found);
        }
        try {
            JSONObject error = new JSONObject(responseBody);
            // {"detail":"Uživatelův e-mail není ověřen."}
            if (code == 403 && error.has(JSON_DETAIL) && error.getString(JSON_DETAIL).contains("e-mail")) {
                abortOperation(R.string.error_email_needs_to_be_validated_before_login);
            }
            if (code == 400 && error.has(JSON_NON_FIELD_ERRORS)) {
                JSONArray errors = error.getJSONArray(JSON_NON_FIELD_ERRORS);
                if (errors.length() >= 1 && errors.get(0).equals(JSON_NON_FIELD_ERRORS_NO_LOGIN)) {
                    abortOperation(R.string.error_could_not_log_in_na_ovoce);
                }
            }
        } catch (JSONException e) {
        }
        if (code != 200 && code != 201 /*POST*/) {
            log.d("code", code);
            abortOperation(R.string.error_unexpected_return_code);
        }
        return new JSONObject(responseBody);
    }

    @Override
    protected Set<String> getUrlsForAllPlants() {
        String host = host();
        HashSet<String> urls = new HashSet<String>();
        for (PlantCategory category: PlantCategory.all()) {
            if (category.canBeUsedByAPI(this)) {
                urls.add(host + "/api/v1/fruit/?kind=" + category.getFieldFor(this));
            }
        }
        return urls;
    }

    protected String host() {
        return Settings.useInsecureConnections() ? "http://na-ovoce.quelltext.eu" : "https://na-ovoce.cz";
    }

    @Override
    protected void addMarkers(String data, Progressable fraction) throws JSONException, ErrorWithExplanation {
        JSONArray json = new JSONArray(data);
        PlantsCache.updateNaOvocePlantMarkers(json, fraction);
    }

    @Override
    public String id() {
        return Settings.API_ID_NA_OVOCE;
    }

    public int radioButtonId(){
        return R.id.radioButton_na_ovoce;
    }

    public String getPlantUrl(String id) {
        return host() + "/fruit/detail/" + id + "/";
    };

    @Override
    public int nameResourceId() {
        return R.string.login_api_name_na_ovoce;
    }
}
