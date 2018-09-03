
function getMarkerIconOfPlant(plant) {
    if (plant.count) {
        return iconForAGroupOfPlants.clone();
    } else {
        var category = plant.properties.tid;
        if (category) {
            return iconForCategory(category);
        }
    }
}

function iconFromName(name, type) {
    var icon = new OpenLayers.Icon({
            anchor: [0.5, 1],
            anchorXUnits: 'fraction',
            anchorYUnits: 'fraction',
            opacity: 0.5,
            src: BASE_ICON_PATH + "/" + name + "." + type,
        });
    return icon;
}

function setIconDescriptionOfMarker(marker, text) {
    var div = marker.icon.imageDiv.appendChild(document.createElement("div"));
    div.innerText = text;
    div.class = "marker-description";
}

var categoryToName = {
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

var catetories = [];

var BASE_ICON_PATH = "../img/markers";

var categoryToIcon = {};
for (var i = 0; i < 100; i++) {
    var category = "" + i;
    var name = categoryToName[category];
    if (name) {
        catetories.push(name);
        var icon = iconFromName(name, "png");
        categoryToIcon[category] = icon;
    }
}
var iconForAGroupOfPlants = iconFromName("group", "svg");


function iconForCategory(category) {
    return categoryToIcon[category].clone();
}



