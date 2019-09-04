
function getMarkerIconOfPlant(plant) {
    if (plant.isCluster) {
        return ICON_FOR_A_GROUP_OF_PLANTS.clone();
    } else {    
        return iconForCategoryId(getCategoryId(plant));
    }
}

function getCategoryId(plant) {
    return plant.properties ? tidToCategoryId[plant.properties.tid] || // using the mundraub API
           plant.properties.category :  // using the offline API
           plant.category || // using the plant schema from this map
           DEFAULT_PLANT_CATEGORY_NAME; // erm. using erm. yeah, maybe there is an error...
}

function iconFromFilename(name) {
    var icon = new OpenLayers.Icon({
            anchor: [0.5, 1],
            anchorXUnits: 'fraction',
            anchorYUnits: 'fraction',
            opacity: 0.5,
            src: BASE_ICON_PATH + "/" + name.replace(/ /g, "-") + ".png",
        });
    return icon;
}

function setIconDescriptionOfMarker(marker, text) {
    var div = marker.icon.imageDiv.appendChild(document.createElement("div"));
    div.innerText = text;
    div.className = "marker-description";
}

// this mapping is used for direct access to the Mundraub API
var tidToCategoryId = {
    "4":  "apple",
    "10": "apricot",
    "5":  "pear",
    "6":  "cherry",
    "7":  "mirabelle",
    "11": "mulberry",
    "8":  "plum",
    "9":  "quince",
    "12": "other fruit trees",
    "18": "blackberry",
    "20": "blueberry",
    "22": "raspberry",
    "21": "elder",
    "27": "rose hip",
    "25": "juneberry",
    "23": "currant",
    "24": "cornel cherry",
    "26": "seaberry",
    "28": "sloe",
    "29": "haw",
    "30": "other fruit shrub",
    "31": "ramsons",
    "33": "mint",
    "34": "rosemary",
    "36": "thyme",
    "32": "juniper",
    "35": "woodruff",
    "37": "other herbs",
    "16": "chestnut",
    "14": "hazel",
    "19": "wild strawberry",
    "15": "walnut",
    "17": "other nut"
};
var DEFAULT_PLANT_CATEGORY_NAME = "unnamed plant"
var BASE_ICON_PATH = "../img/markers";
var ICON_FOR_A_GROUP_OF_PLANTS = iconFromFilename("group");

var categoryIdToIcon = {};

function iconForCategoryId(categoryId) {
    var icon = categoryIdToIcon[categoryId];
    if (!icon) {
        icon = iconFromFilename(categoryId);
        categoryIdToIcon[categoryId] = icon;
    }
    return icon.clone();
}

