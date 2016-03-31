#!/bin/bash


set -e

echo "##### This script installs its libraries into system folders, so please make sure you are running it as a user with proper permissions."
echo "##### Hit any key to continue..."

read ok
echo "##### Installing pocketsphinx...."
rm -rf pocketsphinx
mkdir pocketsphinx
cd pocketsphinx
git clone https://github.com/mozilla/sphinxbase
git clone https://github.com/mozilla/pocketsphinx
cd sphinxbase
./autogen.sh
./configure && make && make install 
cd ../pocketsphinx
./autogen.sh
./configure && make && make install 


echo "##### Generating swig libraries...."
cd swig
make

echo "##### Generating jni module. Make sure you have your JAVA_HOME environment variable correctly set."
cd java
make

echo "##### Generating the maven-nar artifact."
mvn clean install

echo "##### Setting the pocketsphinx model folder into esh properties."
directory=`pwd`
echo "modelpath = $directory/../../../../pocketsphinx/pocketsphinx/model/en-us/" > "$directory/../../../../bundles/io/org.eclipse.smarthome.io.voice/src/main/resources/pocketsphinx.properties"


echo "##### Done! Now do a full maven build of ESH."
