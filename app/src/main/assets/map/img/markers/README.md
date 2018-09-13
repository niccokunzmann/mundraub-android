
This is how to get the marker element from na ovoce

```javascript
function getMarker(hexId) {
    var d = document.getElementById("filter-items").getElementsByTagName("a");
    for (var i =0; i < d.length; i++) {
        var a=d[i];
        if (a.attributes["data-kind"] && a.attributes["data-kind"].value == hexId) {
            console.log(a.children[0]);
        }
    }
}
```

Then, you can inspect the node in Firefox and take a screenshot of it.

This way you can convert the backgroud of white to transparent:
https://stackoverflow.com/questions/9155377/set-transparent-background-using-imagemagick-and-commandline-prompt

To resize:
```
for i in * ; do   convert $i -resize 80x80 ../x/$i; convert ../x/$i -crop 50x80+15+0 ../x/$i ;  done
```

