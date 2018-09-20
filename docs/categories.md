# Categories

There are many categories available to choose your plant for.
The [categories.json] file contains the following fields:
- `categories`  
    The available categories and their mapping to platform ids.
- `order`  
    The order of the ids to display when users choose the plant category.
    
## Category by Example

Here, you can see the category of the apple tree.

    "apple": {              // 1
      "fruitmap": {         // 2
        "id": "Apple tree"  // 3
      },
      "mundraub": {         // 2
        "id": 4             // 3
      },
      "na-ovoce": {         // 2
        "id": "a1e4"        // 3
      },
      "database": {         // 4
        "id": 1             // 5
      }
    }

The lines have the followng meaning:

1. The id used in the app is `"apple"`.
2. The plant can be published on the platforms
    - `"fruitmap"`, see [fruitmap]
    - `"mundraub"`, see [mundraub]
    - `"na-ovoce"`, see [na-ovoce]
3. The id on the platform for this type of plant are:
    - `"fruitmap"`: `"Apple tree"`
    - `"mundraub"`: `4`
    - `"na-ovoce"`: `"a1e4"`
4. There is a mapping so that this category can be saved in the app database.
5. The id in the app database is `1`.
    These ids MUST be unique for each app category.

Some categories are not available for upload on some platforms.

    "tangerine": {
      "fruitmap": {
        "id": "Tangerine"
      },
      "mundraub": {
        "as": "other fruit trees"  // 1
      },
                                   // 2
                                   // 2
                                   // 2
      "database": {
        "id": 47
      }
    },

1. Tangerines cannot be published to `"munraub"` directly.
    Instead, they can be published as the category identified by
    the app id `"other fruit trees"`.
2. Here, you can see that tangerines cannot be uploaded to `na-ovoce`.


## Platforms

Here you can find the values of the platforms and sources.

### Mundraub.org
[mundraub]: #mundraub-org

https://mundraub.org/map

    <optgroup label="Obstbäume"><option value="4">-Apfel</option>
    <option value="10">-Aprikose</option>
    <option value="5">-Birne</option>
    <option value="6">-Kirsche</option>
    <option value="7">-Mirabelle</option>
    <option value="11">-Maulbeere</option>
    <option value="8">-Pflaume</option>
    <option value="9">-Quitte</option>
    <option value="12">-Andere Obstbäume</option>
    </optgroup><optgroup label="Obststräucher"><option value="18">-Brombeere</option>
    <option value="20">-Heidelbeere</option>
    <option value="22">-Himbeere</option>
    <option value="21">-Holunder</option>
    <option value="27">-Hagebutte</option>
    <option value="25">-Felsenbirne</option>
    <option value="23">-Johannisbeere</option>
    <option value="24">-Kornelkirsche</option>
    <option value="26">-Sanddorn</option>
    <option value="28">-Schlehe</option>
    <option value="29">-Weißdorn</option>
    <option value="30">-Andere Obststräucher</option>
    </optgroup><optgroup label="Kräuter"><option value="31">-Bärlauch</option>
    <option value="33">-Minze</option>
    <option value="34">-Rosmarin</option>
    <option value="36">-Thymian</option>
    <option value="32">-Wacholder</option>
    <option value="35">-Waldmeister</option>
    <option value="37">-Andere Kräuter</option>
    </optgroup><optgroup label="Nüsse"><option value="16">-Esskastanie</option>
    <option value="14">-Haselnuss</option>
    <option value="19">-Walderdbeere</option>
    <option value="15">-Walnuss</option>
    <option value="17">-Andere Nüsse</option>
    </optgroup>


### Na-Ovoce.cz
[na-ovoce]: #na-ovoce-cz

https://github.com/jsmesami/naovoce/blob/master/src/fruit/fixtures/kinds.json

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

### FruitMap.org
[fruitmap]: #fruitmap-org

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

https://www.fruitmap.org/api/trees  
does not contain all categories.

    [{"id":"1","name":"Apple tree","color":"#FE3434","count":"2160"},
    {"id":"6","name":"Walnut tree","color":"#CB9668","count":"1533"},
    {"id":"5","name":"Cherry tree","color":"#FF0098","count":"1255"},
    {"id":"4","name":"Plum tree","color":"#67CCCC","count":"800"},
    {"id":"2","name":"Pear tree","color":"#FAF402","count":"626"},
    {"id":"23","name":"Briar","color":"#FF4200","count":"498"},
    {"id":"17","name":"Elderberry","color":"#FFFFD3","count":"391"},
    {"id":"38","name":"Yellow Plum","color":"#FFD800","count":"385"},
    {"id":"9","name":"Hazelnut","color":"#34CC01","count":"325"},
    {"id":"7","name":"Mulberry","color":"#CC0101","count":"292"},
    {"id":"22","name":"Blackberry","color":"#7C2424","count":"235"},
    {"id":"13","name":"Peach tree","color":"#F7B46F","count":"215"},
    {"id":"20","name":"Chestnut","color":"#64481A","count":"157"},
    {"id":"12","name":"Fig","color":"#FD6601","count":"156"},
    {"id":"10","name":"Horse chestnut","color":"#989866","count":"148"},
    {"id":"40","name":"Hawthorn","color":"#E7344C","count":"147"},
    {"id":"29","name":"Lemon","color":"#FFF500","count":"147"},
    {"id":"14","name":"Strawberries","color":"#FF0005","count":"126"},
    {"id":"31","name":"Bilberries","color":"#A04DB2","count":"123"},
    {"id":"8","name":"Sour cherry","color":"#CB0032","count":"110"},
    {"id":"45","name":"Wild Garlic","color":"#669D24","count":"106"},
    {"id":"21","name":"Raspberry","color":"#CB00A7","count":"94"},
    {"id":"28","name":"Orange","color":"#F57E17","count":"89"},
    {"id":"24","name":"Grape","color":"#08FF3C","count":"84"},
    {"id":"16","name":"Greengage","color":"#836FA0","count":"67"},
    {"id":"39","name":"Blackthorn","color":"#4565AD","count":"58"},
    {"id":"3","name":"Apricot tree","color":"#FD9834","count":"48"},
    {"id":"11","name":"Red ribes","color":"#CE435D","count":"46"},
    {"id":"36","name":"Rowan","color":"#F15B40","count":"44"},
    {"id":"33","name":"Chokeberry","color":"#21409A","count":"41"},
    {"id":"41","name":"Sea Buckthorn","color":"#F07E26","count":"37"}]

[categories.json]: ../app/src/main/assets/categories/categories.json
