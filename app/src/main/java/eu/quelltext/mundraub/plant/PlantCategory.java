package eu.quelltext.mundraub.plant;

import android.content.Intent;
import android.util.Log;

import java.io.Console;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.R;

public class PlantCategory {

    private final int resourceId;
    private final int fieldForAPI;
    private final String id;
    private static final String INTENT_FIELD = "PlantCategory";

    private static Map<String, PlantCategory> idToPlantCategory = new HashMap<>();
    private static Map<Integer, PlantCategory> fieldToPlantCategory = new HashMap<>();

    private static void addCategory(int fieldForAPI, String id, int resourceId) {
        PlantCategory category = new PlantCategory(id, fieldForAPI, resourceId);
        idToPlantCategory.put(id, category);
        fieldToPlantCategory.put(fieldForAPI, category);
    }

    public static Collection<PlantCategory> all() {
        return idToPlantCategory.values();
    }

    static {
        // from https://mundraub.org/map
        //<optgroup label="Obstbäume"><option value="4">-Apfel</option>
        addCategory(4, "apple", R.string.apple);
        // <option value="10">-Aprikose</option>
        addCategory(10, "apricot", R.string.apricot);
        // <option value="5">-Birne</option>
        addCategory(5, "pear", R.string.pear);
        // <option value="6">-Kirsche</option>
        addCategory(6, "cherry", R.string.cherry);
        // <option value="7">-Mirabelle</option>
        addCategory(7, "mirabelle", R.string.mirabelle);
        // <option value="11">-Maulbeere</option>
        addCategory(11, "mulberry", R.string.mulberry);
        // <option value="8">-Pflaume</option>
        addCategory(8, "plum", R.string.plum);
        // <option value="9">-Quitte</option>
        addCategory(9, "quince", R.string.quince);
        // <option value="12">-Andere Obstbäume</option>
        addCategory(12, "other fruit trees", R.string.other_fruit_trees);
        // </optgroup><optgroup label="Obststräucher"><option value="18">-Brombeere</option>
        addCategory(18, "blackberry", R.string.blackberry);
        // <option value="20">-Heidelbeere</option>
        addCategory(20, "blueberry", R.string.blueberry);
        // <option value="22">-Himbeere</option>
        addCategory(22, "raspberry", R.string.raspberry);
        // <option value="21">-Holunder</option>
        addCategory(21, "elder", R.string.elder);
        // <option value="27">-Hagebutte</option>
        addCategory(27, "rose hip", R.string.rose_hip);
        // <option value="25">-Felsenbirne</option>
        addCategory(25, "juneberry", R.string.juneberry);
        // <option value="23">-Johannisbeere</option>
        addCategory(23, "currant", R.string.currant);
        // <option value="24">-Kornelkirsche</option>
        addCategory(24, "cornel cherry", R.string.cornel_cherry);
        // <option value="26">-Sanddorn</option>
        addCategory(26, "seaberry", R.string.seaberry);
        // <option value="28">-Schlehe</option>
        addCategory(28, "sloe", R.string.sloe);
        // <option value="29">-Weißdorn</option>
        addCategory(29, "haw", R.string.haw);
        // <option value="30">-Andere Obststräucher</option>
        addCategory(30, "other fruit shrub", R.string.other_fruit_shrub);
        // </optgroup><optgroup label="Kräuter"><option value="31">-Bärlauch</option>
        addCategory(31, "ramsons", R.string.ramsons);
        // <option value="33">-Minze</option>
        addCategory(33, "mint", R.string.mint);
        // <option value="34">-Rosmarin</option>
        addCategory(34, "rosemary", R.string.rosemary);
        // <option value="36">-Thymian</option>
        addCategory(36, "thyme", R.string.thyme);
        // <option value="32">-Wacholder</option>
        addCategory(32, "juniper", R.string.juniper);
        // <option value="35">-Waldmeister</option>
        addCategory(35, "woodruff", R.string.woodruff);
        // <option value="37">-Andere Kräuter</option>
        addCategory(37, "other herbs", R.string.other_herbs);
        // </optgroup><optgroup label="Nüsse"><option value="16">-Esskastanie</option>
        addCategory(16, "chestnut", R.string.chestnut);
        // <option value="14">-Haselnuss</option>
        addCategory(14, "hazel", R.string.hazel);
        // <option value="19">-Walderdbeere</option>
        addCategory(19, "wild strawberry", R.string.wild_strawberry);
        // <option value="15">-Walnuss</option>
        addCategory(15, "walnut", R.string.walnut);
        // <option value="17">-Andere Nüsse</option>
        addCategory(17, "other nut", R.string.other_nut);
        // </optgroup>
    }

    PlantCategory(String id, int fieldForAPI, int resourceId) {
        this.id = id;
        this.fieldForAPI = fieldForAPI;
        this.resourceId = resourceId;
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

    public static class Intent extends android.content.Intent {

        public Intent(PlantCategory category) {
            super();
            this.putExtra(INTENT_FIELD, category.getIntentIdetifier());
        }
    }

    public static PlantCategory fromIntent(android.content.Intent intent) {
        return idToPlantCategory.get(intent.getStringExtra(INTENT_FIELD));
    }
}
