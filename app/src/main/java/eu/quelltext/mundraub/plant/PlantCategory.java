package eu.quelltext.mundraub.plant;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.ErrorAware;

public class PlantCategory extends ErrorAware {

    private final int resourceId;
    private final int fieldForMundraubAPI;
    private final int fieldForNaOvoceAPI;
    private final String id;
    private final int databaseId;
    private static final String INTENT_FIELD = "PlantCategory";

    public static final PlantCategory NULL = new NullCategory();
    private static Map<String, PlantCategory> idToPlantCategory = new HashMap<>();
    private static Map<Integer, PlantCategory> mundraubAPIFieldToPlantCategory = new HashMap<Integer, PlantCategory>();
    private static Map<Integer, PlantCategory> naOvoceAPIFieldToPlantCategory = new HashMap<Integer, PlantCategory>();
    private static Map<Integer, PlantCategory> databaseIdToPlantCategory = new HashMap<Integer, PlantCategory>();
    private static List<PlantCategory> sortedCategories = new ArrayList<PlantCategory>();
    private static List<PlantCategory> metaCategories = new ArrayList<PlantCategory>();
    private Drawable markerDrawable = null;
    private boolean triedGettingMarkerDrawable = false;

    private static PlantCategory addCategory(int databaseId, String fieldForFruitMap, int fieldForNoOvoceAPI, int fieldForMundraubAPI, String id, int resourceId) {
        PlantCategory category = new PlantCategory(id, fieldForMundraubAPI, fieldForNoOvoceAPI, resourceId, databaseId);
        idToPlantCategory.put(id, category);
        mundraubAPIFieldToPlantCategory.put(fieldForMundraubAPI, category);
        assert !databaseIdToPlantCategory.containsKey(databaseId);
        databaseIdToPlantCategory.put(databaseId, category);
        naOvoceAPIFieldToPlantCategory.put(fieldForNoOvoceAPI, category);
        sortedCategories.add(category);
        return category;
    }

    public static Collection<PlantCategory> all() {
        return new ArrayList<PlantCategory>(sortedCategories);
    }
    /* https://github.com/jsmesami/naovoce/blob/master/src/fruit/fixtures/kinds.json
        0xa1e4 Apple Tree
        0xa3b8 Pear Tree
        0xa412 Cherry Tree
        0xa3a2 Sour Cherry
        0xa1d1 Yellow Plum // https://en.wikipedia.org/wiki/Yellow_plum
        0xa2c9 Plum Tree
        0xa13b Apricot Tree
        0xa31a Quince
        0xa0d5 Blackberry
        0xa349 Raspberry
        0xa430 Elderberry
        0xa398 Wild Rose
        0xa477 Bilberry
        0xa1f4 Sea Buckthorn // https://en.wikipedia.org/wiki/Hippophae
        0xa0a7 Blackthorn // https://de.wikipedia.org/wiki/Schlehdorn
        0xa272 Hawthorn
        0xa118 Mint
        0xa324 Lemon Balm
        0xa3c4 Hazel Tree
        0xa03b Walnut Tree
        0xa08d Chestnut Tree
        0xa205 Ribes // https://en.wikipedia.org/wiki/Ribes
        0xa162 Mulberry Tree
        0xa11b Wild Strawberry
        0xa1bb Wild Garlic // https://cs.wikipedia.org/wiki/%C4%8Cesnek_medv%C4%9Bd%C3%AD -> https://en.wikipedia.org/wiki/Allium_ursinum -> ramsons
        0xa116 Cornel
        0xa09b Chokeberry // https://de.wikipedia.org/wiki/Apfelbeeren
        0xa0c9 Medlar // https://de.wikipedia.org/wiki/Mispel
        0xa277 Service Tree // https://de.wikipedia.org/wiki/Mehlbeeren
        0xa2b2 Rowan // much like service tree
        0xa485 Almond tree
        0xa43a Gooseberry
        0xa382 Peach Tree
        0xa3d3 Silverberry // https://de.wikipedia.org/wiki/Silber-%C3%96lweide

        FruitMap.org:
        https://github.com/niccokunzmann/mundraub-android/issues/108
         'Briar',
         'Hawthorn',
         'Tangerine',
         'Grapefruit',
         'Plum tree',
         'Yellow Plum',
         'Raspberry',
         'Apple tree',
         'Hazelnut',
         'Pear tree',
         'Red ribes',
         'Cherry tree',
         'Sour cherry',
         'Peach tree',
         'Apricot tree',
         'Medlar',
         'Dogwood', // https://dict.leo.org/german-english/Hartriegel -> https://dict.leo.org/german-english/cornel
         'Strawberries',
         'Josta',
         'Greengage',
         'Grape',
         'Orange',
         'Chokeberry',
         'Cornel',
         'Sea Buckthorn',
         'Service Tree',
         'Lemon',
         'Gooseberry',
         'Walnut tree',
         'Almond',
         'Blackthorn',
         'Chestnut',
         'Wild Garlic',
         'Silverberry',
         'Black ribes',
         'Bilberries',
         'Mint',
         'Quince',
         'Blackberry',
         'Fig',
         'Lemon Balm',
         'Elderberry',
         'Wild Strawberry',
         'Mulberry',
         'Horse chestnut',
         'Rowan'
     */

    static {
        // never change the database id field!
        // from https://mundraub.org/map
        //<optgroup label="Obstbäume"><option value="4">-Apfel</option>
        addCategory(1, "Apple tree", 0xa1e4, 4, "apple", R.string.apple);
        // <option value="10">-Aprikose</option>
        addCategory(2, "Apricot tree", 0xa13b, 10, "apricot", R.string.apricot);
        // <option value="5">-Birne</option>
        addCategory(3, "Pear tree", 0xa3b8, 5, "pear", R.string.pear);
        // <option value="6">-Kirsche</option>
        addCategory(4, "Cherry tree", 0xa412, 6, "cherry", R.string.cherry);
        addCategory(5, "Sour cherry", 0xa3a2, 6, "sour cherry", R.string.sour_cherry);
        // <option value="7">-Mirabelle</option>
        addCategory(6, "Yellow Plum", 0xa1d1, 7, "mirabelle", R.string.mirabelle);
        // <option value="11">-Maulbeere</option>
        addCategory(7, "Mulberry", 0xa162, 11, "mulberry", R.string.mulberry);
        // <option value="8">-Pflaume</option>
        addCategory(8, "Plum tree", 0xa2c9, 8, "plum", R.string.plum);
        addCategory(53, "Greengage", 0xa2c9, 8, "greengage", R.string.greengage);
        // <option value="9">-Quitte</option>
        addCategory(9, "Quince", 0xa31a, 9, "quince", R.string.quince);
        // <option value="12">-Andere Obstbäume</option>
        addCategory(11, "Service Tree", 0xa277, 12, "service tree", R.string.service_tree);
        addCategory(12, "Rowan", 0xa2b2, 12, "rowan", R.string.rowan);
        addCategory(13, "Peach tree", 0xa382, 12, "peach", R.string.peach);
        addCategory(28, "Chokeberry", 0xa09b, 12, "chokeberry", R.string.chokeberry);
        addCategory(29, "Medlar", 0xa0c9, 12, "medlar", R.string.medlar);
        addCategory(47, "Tangerine", 0, 12, "tangerine", R.string.tangerine).plantMapOnly();
        addCategory(48, "Grapefruit", 0, 12, "grapefruit", R.string.grapefruit).plantMapOnly();
        addCategory(55, "Orange", 0, 12, "orange", R.string.orange).plantMapOnly();
        addCategory(57, "Lemon", 0, 12, "lemon", R.string.lemon).plantMapOnly();
        addCategory(59, "Fig", 0, 12, "fig", R.string.fig).plantMapOnly();
        addCategory(10, "", 0, 12, "other fruit trees", R.string.other_fruit_trees).meta();
        // </optgroup><optgroup label="Obststräucher"><option value="18">-Brombeere</option>
        addCategory(14, "Blackberry", 0xa0d5, 18, "blackberry", R.string.blackberry);
        addCategory(58, "Briar", 0xa0d5, 18, "briar", R.string.briar).plantMapOnly();
        // <option value="20">-Heidelbeere</option>
        addCategory(15, "Bilberries", 0xa477, 20, "blueberry", R.string.blueberry);
        //addCategory(16, "Bilberries", 0xa477, 20, "bilberry", R.string.bilberry);
        // <option value="22">-Himbeere</option>
        addCategory(17, "Raspberry", 0xa349, 22, "raspberry", R.string.raspberry);
        // <option value="21">-Holunder</option>
        addCategory(18, "Elderberry", 0xa430, 21, "elder", R.string.elder);
        // <option value="27">-Hagebutte</option>
        addCategory(19, "", 0xa398, 27, "rose hip", R.string.rose_hip);
        // <option value="25">-Felsenbirne</option>
        addCategory(20, "", 0, 25, "juneberry", R.string.juneberry);
        // <option value="23">-Johannisbeere</option>
        addCategory(21, "", 0xa205, 23, "currant", R.string.currant);
        addCategory(49, "Red ribes", 0xa205, 23, "red ribes", R.string.red_ribes).plantMapOnly();
        addCategory(50, "Black ribes", 0xa205, 23, "black ribes", R.string.black_ribes).plantMapOnly();
        addCategory(52, "Josta", 0xa205, 23, "jostaberry", R.string.jostaberry).plantMapOnly();
        // <option value="24">-Kornelkirsche</option>
        addCategory(56, "Dogwood", 0xa116, 24, "dogwood", R.string.dogwood); // sub of cornel
        addCategory(22, "Cornel", 0xa116, 24, "cornel cherry", R.string.cornel_cherry);
        // <option value="26">-Sanddorn</option>
        addCategory(23, "Sea Buckthorn", 0xa1f4, 26, "seaberry", R.string.seaberry);
        // <option value="28">-Schlehe</option>
        addCategory(24, "Blackthorn", 0xa0a7, 28, "sloe", R.string.sloe);
        // <option value="29">-Weißdorn</option>
        addCategory(25, "Hawthorn", 0xa272, 29, "haw", R.string.haw);
        // <option value="30">-Andere Obststräucher</option>
        addCategory(30, "Gooseberry", 0xa43a, 30, "gooseberry", R.string.gooseberry);
        addCategory(54, "Grape", 0, 0, "grape", R.string.grape).plantMapOnly();
        addCategory(26, "", 0, 30, "other fruit shrub", R.string.other_fruit_shrub).meta();
        // </optgroup><optgroup label="Kräuter"><option value="31">-Bärlauch</option>
        addCategory(32, "Wild Garlic", 0xa1bb, 31, "ramsons", R.string.ramsons);
        // <option value="33">-Minze</option>
        addCategory(33, "Mint", 0xa118, 33, "mint", R.string.mint);
        // <option value="34">-Rosmarin</option>
        addCategory(34, "", 0, 34, "rosemary", R.string.rosemary);
        // <option value="36">-Thymian</option>
        addCategory(35, "", 0, 36, "thyme", R.string.thyme);
        // <option value="32">-Wacholder</option>
        addCategory(36, "", 0, 32, "juniper", R.string.juniper);
        // <option value="35">-Waldmeister</option>
        addCategory(37, "", 0, 35, "woodruff", R.string.woodruff);
        // <option value="37">-Andere Kräuter</option>
        addCategory(31, "Silverberry", 0xa3d3, 37, "silverberry", R.string.silverberry);
        addCategory(39, "Lemon Balm", 0xa324, 37, "lemon balm", R.string.lemon_balm); // Zitronenmelisse
        addCategory(38, "", 0, 37, "other herbs", R.string.other_herbs).meta();
        // </optgroup><optgroup label="Nüsse"><option value="16">-Esskastanie</option>
        addCategory(41, "Chestnut", 0xa08d, 16, "chestnut", R.string.chestnut);
        addCategory(60, "Horse chestnut", 0xa08d, 16, "horse chestnut", R.string.horse_chestnut);
        // <option value="14">-Haselnuss</option>
        addCategory(42, "Hazelnut", 0xa3c4, 14, "hazel", R.string.hazel);
        // <option value="19">-Walderdbeere</option>
        addCategory(43, "Wild Strawberry", 0xa11b, 19, "wild strawberry", R.string.wild_strawberry);
        addCategory(51, "Strawberries", 0xa11b, 19, "strawberry", R.string.strawberry).plantMapOnly();
        // <option value="15">-Walnuss</option>
        addCategory(44, "Walnut tree", 0xa03b, 15, "walnut", R.string.walnut);
        // <option value="17">-Andere Nüsse</option>
        addCategory(46, "Almond", 0xa485, 17, "almond", R.string.almond);
        addCategory(45, "", 0, 17, "other nut", R.string.other_nut).meta();
        // </optgroup>

        // give preference to meta data
        for (PlantCategory category : metaCategories) {
            mundraubAPIFieldToPlantCategory.put(category.fieldForMundraubAPI, category);
        }
    }

    private void plantMapOnly() {
        sortedCategories.remove(this);
    }

    /*
     * Meta category which unites other categories.
     */
    private void meta() {
        metaCategories.add(this);
    }

    PlantCategory(String id, int fieldForMundraubAPI, int fieldForNaOvoceAPI, int resourceId, int databaseId) {
        this.id = id;
        this.fieldForMundraubAPI = fieldForMundraubAPI;
        this.resourceId = resourceId;
        this.fieldForNaOvoceAPI = fieldForNaOvoceAPI;
        this.databaseId = databaseId;
    }

    // ------- construct the plant category from different representations -------

    public static PlantCategory fromMundraubAPIField(int i) {
        if (mundraubAPIFieldToPlantCategory.containsKey(i)) {
            return mundraubAPIFieldToPlantCategory.get(i);
        }
        return NULL;
    }

    public static PlantCategory fromNaOvoceAPIField(int i) {
        if (naOvoceAPIFieldToPlantCategory.containsKey(i)) {
            return naOvoceAPIFieldToPlantCategory.get(i);
        }
        return NULL;
    }

    public static PlantCategory fromDatabaseId(int i) {
        if (databaseIdToPlantCategory.containsKey(i)) {
            return databaseIdToPlantCategory.get(i);
        }
        return NULL;
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

    public static class NullCategory extends PlantCategory {

        NullCategory() {
            super(null, -1, 0, R.string.unnamed_plant, -1);
        }
        public boolean isUnknown() {
            return true;
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

    public String getValueForMundraubAPI() {
        return Integer.toString(fieldForMundraubAPI);
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getValueForNaOvoceAPI() {
        return Integer.toHexString(fieldForNaOvoceAPI);
    }


    @Override
    public String toString() {
        // TODO: return a translatable R.id
        return this.id;
    }

    public String getIntentIdetifier() {
        return this.id;
    }

    public int getResourceId() {
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

    public boolean canBeUsedByNaOvoce() {
        return !isUnknown() && fieldForNaOvoceAPI != 0;
    }

    public boolean canBeUsedByMundraub() {
        return !isUnknown() && fieldForMundraubAPI != 0;
    }

}
