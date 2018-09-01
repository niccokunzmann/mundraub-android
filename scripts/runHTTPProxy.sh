#!/bin/bash

set -e

cd "`dirname \"$0\"`"

PORT="$1"

cd ..

ROOT="`pwd`"
LIBS="$ROOT/app/libs"
for lib in "$LIBS"/*; do
    export CLASSPATH="$lib:$CLASSPATH"
done

cd app/src/main/java

javac eu/quelltext/mundraub/map/MundraubMapAPI.java

#(
#    sleep 0.2
#    wget -O- 'http://localhost:'"$PORT"'/plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37'
#) &

java eu.quelltext.mundraub.map.MundraubMapAPI "$PORT"


