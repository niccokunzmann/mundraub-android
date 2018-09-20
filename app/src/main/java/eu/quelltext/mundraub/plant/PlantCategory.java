package eu.quelltext.mundraub.plant;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.common.Dialog;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;

public class PlantCategory {

    private static final String INTENT_FIELD = "PlantCategory";
    private static final String FILE_CATEGORIES = "categories/categories.json";

    private final String id;
    private Drawable markerDrawable = null;
    private boolean triedGettingMarkerDrawable = false;
    private Map<String, String> apiIdToField = new HashMap<>();
    private Map<String, PlantCategory> apiIdToCategory = new HashMap<String, PlantCategory>();

    public static final PlantCategory NULL = new NullCategory();
    public static PlantCategory EXAMPLE = NULL;
    private static Map<String, PlantCategory> idToPlantCategory = new HashMap<String, PlantCategory>();
    private static Map<String, Map<String, PlantCategory>> apiIdToFieldToPlantCategory = new HashMap<>();
    private static List<PlantCategory> sortedCategories = new ArrayList<PlantCategory>();
    private static final Logger.Log log = Logger.newFor("PlantCategory");
    private static Activity activity;

    private static final String API_ID_DATABASE = "database";
    private static final String API_ID_COMMUNITY = Settings.API_ID_COMMUNITY;
    private static final String API_ID_MUNDRAUB = API.MUNDRAUB.id();
    private static final String API_ID_NA_OVOCE = API.NA_OVOCE.id();
    private static final String API_ID_FRUITMAP = API.FRUITMAP.id();

    public static Collection<PlantCategory> all() {
        return new ArrayList<PlantCategory>(sortedCategories);
    }

    public static Collection<PlantCategory> allVisible() {
        ArrayList<PlantCategory> result = new ArrayList<PlantCategory>();
        for (PlantCategory category: all()) {
            if (category.isVisible()) {
                result.add(category);
            }
        }
        return result;
    }

    static {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(final Activity context) {
                activity = context;
                try {
                    JSONObject json = loadJSONFromAsset(context, FILE_CATEGORIES);
                    JSONObject categories = json.getJSONObject(JSON_CATEGORIES);
                    JSONArray order = json.getJSONArray(JSON_ORDER);
                    createCategoriesFromJSON(categories);
                    mapCategoriesFromJSON(categories);
                    fillSortedCategoryCollection(order);
                    return;
                } catch (IOException e) {
                    log.printStackTrace(e);
                } catch (JSONException e) {
                    log.printStackTrace(e);
                }
                new Dialog(context).askYesNo(
                        R.string.reason_plant_categories_could_not_load,
                        R.string.plant_categories_could_not_load_ask_close,
                        new Dialog.YesNoCallback() {
                            @Override
                            public void yes() {
                                context.finish();
                            }

                            @Override
                            public void no() {
                            }
                        });
            }
        });
    }

    private static void fillSortedCategoryCollection(JSONArray order) throws JSONException {
        for (int i = 0; i < order.length(); i++) {
            String id = order.getString(i);
            sortedCategories.add(withId(id));
        }
        for (PlantCategory category: idToPlantCategory.values()) {
            if (!sortedCategories.contains(category)) {
                sortedCategories.add(category);
            }
        }
    }

    private static final String JSON_CATEGORIES = "categories";
    private static final String JSON_ORDER = "order";
    private static final String JSON_ID = "id";
    private static final String JSON_AS = "as";

    private static void createCategoriesFromJSON(JSONObject categories) throws JSONException {
        for (Iterator<String> it = categories.keys(); it.hasNext(); ) {
            String id = it.next();
            idToPlantCategory.put(id, new PlantCategory(id));
        }
    }
    private static void mapCategoriesFromJSON(JSONObject categories) throws JSONException {
        for (Iterator<String> it = categories.keys(); it.hasNext(); ) {
            String id = it.next();
            JSONObject JSONcategory = categories.getJSONObject(id);
            PlantCategory category = withId(id);
            for (Iterator<String> it2 = JSONcategory.keys(); it2.hasNext(); ) {
                String apiId = it2.next();
                setMapping(categories, apiId, JSONcategory, category, category);
            }
        }
    }

    private static void setMapping(
            JSONObject categories, String apiId, JSONObject category,
            PlantCategory sourceCategory, PlantCategory mappedCategory
    ) throws JSONException {
        JSONObject mapping = category.getJSONObject(apiId);
        if (mapping.has(JSON_ID)) {
            // we arrived at the final category
            String field = mapping.optString(JSON_ID, "");
            if (field.isEmpty()) {
                field = Integer.toString(mapping.getInt(JSON_ID));
            }
            sourceCategory.setAPIMapping(apiId, field, mappedCategory);
        } else if (mapping.has(JSON_AS)) {
            String asId = mapping.getString(JSON_AS);
            setMapping(categories, apiId, categories.getJSONObject(asId), sourceCategory,
                    idToPlantCategory.get(asId));
        }
    }

    private void setAPIMapping(String apiId, String field, PlantCategory mappedCategory) {
        apiIdToCategory.put(apiId, mappedCategory);
        apiIdToField.put(apiId, field);
        if (!apiIdToFieldToPlantCategory.containsKey(apiId)) {
            apiIdToFieldToPlantCategory.put(apiId, new HashMap<String, PlantCategory>());
        }
        if (mappedCategory == this) {
            apiIdToFieldToPlantCategory.get(apiId).put(field, this);
        }
    }


    public static JSONObject loadJSONFromAsset(Context context, String path) throws IOException, JSONException {
        // from https://stackoverflow.com/a/13814551/1320237
        String json = null;
        InputStream is = context.getAssets().open(path);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        return new JSONObject(json);

    }

    PlantCategory(String id) {
        this.id = id;
        EXAMPLE = this;
    }

    // ------- construct the plant category from different representations -------
    private static PlantCategory fromAPIField(String apiId, String field) {
        if (apiIdToFieldToPlantCategory.containsKey(apiId)) {
            Map<String, PlantCategory> fieldToPlantCategory = apiIdToFieldToPlantCategory.get(apiId);
            if (fieldToPlantCategory.containsKey(field)) {
                return fieldToPlantCategory.get(field);
            }
        }
        return NULL;
    }

    public static PlantCategory fromMundraubAPIField(int field) {
        return fromAPIField(API_ID_MUNDRAUB, Integer.toString(field));
    }

    public static PlantCategory fromNaOvoceAPIField(String field) {
        return fromAPIField(API_ID_NA_OVOCE, field);
    }

    public static PlantCategory fromFruitMapAPIField(String field) {
        return fromAPIField(API_ID_FRUITMAP, field);
    }

    public static PlantCategory fromDatabaseId(int field) {
        return fromAPIField(API_ID_DATABASE, Integer.toString(field));
    }

    public static PlantCategory withId(String id) {
        if (id == null) {
            return NULL;
        }
        if (idToPlantCategory.containsKey(id)) {
            return idToPlantCategory.get(id);
        }
        return NULL;
    }

    public static PlantCategory fromIntent(android.content.Intent intent) {
        return withId(intent.getStringExtra(INTENT_FIELD));
    }

    public PlantCategory on(API api) {
        if (apiIdToCategory.containsKey(api.id())) {
            return apiIdToCategory.get(api.id());
        }
        return NULL;
    }

    public static class NullCategory extends PlantCategory {

        NullCategory() {
            super("null");
        }
        public boolean isUnknown() {
            return true;
        }

        @Override
        public int getResourceId() {
            return R.string.unnamed_plant;
        }
    }

    // ------- create different representations of the plant category  -------

    public static class Intent extends android.content.Intent {

        public Intent(PlantCategory category) {
            super();
            this.putExtra(INTENT_FIELD, category.getIntentIdetifier());
        }
    }

    public String getId() {
        return this.id;
    }

    public int getDatabaseId() {
        return Integer.parseInt(apiIdToField.get(API_ID_DATABASE));
    }

    public String getFieldFor(API api) {
        return apiIdToField.get(api.id());
    }

    @Override
    public String toString() {
        return this.id;
    }

    public String getIntentIdetifier() {
        return this.id;
    }

    public int getResourceId() {
        int resourceId = activity.getResources().getIdentifier(id.replaceAll(" ", "_"), "string", activity.getPackageName());
        if (resourceId == 0) {
            // android.content.res.Resources$NotFoundException: String resource ID #0x0
            return R.string.fruit_resource_id_not_found;
        }
        return resourceId;
    }

    public void setMarkerImageOrHide(ImageView imageView) {
        // set image to ImageView
        Drawable drawable = getMarkerDrawable(imageView.getContext());
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private Drawable getMarkerDrawable(Context context) {
        if (triedGettingMarkerDrawable) {
            return markerDrawable;
        }
        triedGettingMarkerDrawable = true;
        try {
            // load image from asstes from https://stackoverflow.com/a/11734850
            // get input stream
            InputStream ims = context.getAssets().open("map/img/markers/" + id.replaceAll(" ", "-") + ".png");
            // load image as Drawable
            Drawable drawable = Drawable.createFromStream(ims, null);
            ims.close();
            markerDrawable = drawable;
        } catch (IOException e) {
            log.e("getMarkerDrawable", "No marker for " + this.id);
        }
        return markerDrawable;
    }



    // ------- asking the category -------

    public boolean isUnknown() {
        return false;
    }

    private boolean canBeUsedByAPI(String apiId) {
        return !isUnknown() && apiIdToCategory.containsKey(apiId);
    }

    private boolean isForAPI(String apiId) {
        return canBeUsedByAPI(apiId) && apiIdToCategory.get(apiId) == this;
    }

    private boolean isForCommunity() {
        for (String apiId: apiIdToCategory.keySet()) {
            if (apiId.equals(API_ID_DATABASE)) {
                continue;
            }
            if (apiIdToCategory.get(apiId) == this) {
                return false;
            }
        }
        return true;
    }

    public boolean canBeUsedByAPI(API api) {
        return canBeUsedByAPI(api.id());
    }

    private boolean isVisible() {
        if (Settings.showCategory(API_ID_COMMUNITY) && isForCommunity()) {
            return true;
        }
        for (String apiId: apiIdToCategory.keySet()) {
            if (Settings.showCategory(apiId) && isForAPI(apiId)) {
                return true;
            }
        }
        return false;
    }
}
