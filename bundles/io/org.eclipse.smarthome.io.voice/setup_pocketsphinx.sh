#!/bin/bash

root=`pwd`
set -e

echo "##### Fetching pocketsphinx..."
rm -rf pocketsphinx
git clone --depth 1 https://github.com/mozilla/pocketsphinx
rm -rf pocketsphinx/.git

echo "##### Building pocketsphinx...."
cd pocketsphinx
./build-java-bindings.sh
cd ..

echo "##### Copying artifacts...."
cp -f pocketsphinx/swig/java/libpocketsphinx_jni.* lib
cp -f pocketsphinx/swig/java/pocketsphinx.jar lib
cp -f pocketsphinx/swig/java/libpocketsphinx_jni.* ../org.eclipse.smarthome.io.voice.test/lib
cp -f pocketsphinx/swig/java/pocketsphinx.jar ../org.eclipse.smarthome.io.voice.test/lib
cp -rf pocketsphinx/model model
echo "modelpath = ${root}/pocketsphinx/model" > "${root}/src/main/resources/pocketsphinx.properties"
