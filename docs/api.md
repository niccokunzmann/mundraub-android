# API

This document describes the API provided by Mundraub.org.

# Markers

Mundraub.org can be queried for markers.

> https://mundraub.org/cluster/plant?bbox=15.615354180336,50.70960935671239,15.623878240585329,50.7105537197353&zoom=18&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37

This URL has several components
- `https://mundraub.org/cluster/plant`  
  this is the path to the endpoint
- `bbox=15.615354180336,50.70960935671239,15.623878240585329,50.7105537197353`  
  is a concatenation separated by commas of the bounding box.
  Assumption: lowest values go first. `minLongitude,minLatitude,maxLongitude,maxLatitude`.
- `zoom=18`  
  is the OpenLayers zoom level. On all levels except 18, the API may return
  clusters of plants if there are many plants in one place.
- `cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37`  
  are the [categories] to query.
  
This is a result which shows one known plant with
- [`tid` category][categories] 20 blueberry 
- node id `950` to construct the plant url
```json
{
    "features": [
        {
            "pos": ["50.7101020812990000","15.6198978424070000"],
            "properties":{
                "nid":"950",
                "tid":"20"
            }
        }
    ]
}
```

This is a result which shows a cluster of two plants:
```json
{
    "features": [
        {
            "pos": [50.059201525271,17.197637557984],
            "count": 2
        }
    ]
}
```
  
## Categories
[categories]: #categories

Mundraub supports several categories of plants.
The listing below shows the API field or `tid` of the plant and the
app internal id.

- 4 - `apple`
- 10 - `apricot`
- 5 - `pear`
- 6 - `cherry`
- 7 - `mirabelle`
- 11 - `mulberry`
- 8 - `plum`
- 9 - `quince`
- 12 - `other fruit trees`
- 18 - `blackberry`
- 20 - `blueberry`
- 22 - `raspberry`
- 21 - `elder`
- 27 - `rose hip`
- 25 - `juneberry`
- 23 - `currant`
- 24 - `cornel cherry`
- 26 - `seaberry`
- 28 - `sloe`
- 29 - `haw`
- 30 - `other fruit shrub`
- 31 - `ramsons`
- 33 - `mint`
- 34 - `rosemary`
- 36 - `thyme`
- 32 - `juniper`
- 35 - `woodruff`
- 37 - `other herbs`
- 16 - `chestnut`
- 14 - `hazel`
- 19 - `wild strawberry`
- 15 - `walnut`
- 17 - `other nut`

The app can support more categories of plants if they are mapped to the
corresponding existing category such as `other fruit trees`, `other fruit shrub`,
`other herbs` and `other nut`.



