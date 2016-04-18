#!/bin/bash


set -e

echo "##### This script installs pocketsphinx libraries for Vaani-Iot."
git submodule init
git submodule update
cd pocketsphinx/sphinxbase
git pull origin master
./autogen.sh
./configure --prefix=`pwd`/../../bundles/io/org.eclipse.smarthome.io.voice/lib/ps/ && make && make install 
cd ../pocketsphinx
git pull origin master
./autogen.sh
./configure --prefix=`pwd`/../../bundles/io/org.eclipse.smarthome.io.voice/lib/ps/ && make && make install 

export LD_RUN_PATH=`pwd`/../../bundles/io/org.eclipse.smarthome.io.voice/lib/ps/lib/
echo "#### LD_RUN_PATH set to: $LD_RUN_PATH"


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
echo "modelpath = $directory/../../../../pocketsphinx/pocketsphinx/model/en-us/" > "$directory/../../../../bundles/io/org.eclipse.smarthome.io.voice/src/main/resources/pocketsphinx_en_US.properties"

unset LD_RUN_PATH

echo "##### Done! Now do a full maven build of ESH."
