#!/bin/bash

root=`pwd`
set -e

echo "##### This script installs pocketsphinx libraries for Vaani-Iot."
git submodule init
git submodule update
cd pocketsphinx/sphinxbase
git pull origin master
./autogen.sh
./configure --prefix=${root}/lib/ps/ && make && make install 
cd ../pocketsphinx
git pull origin master
./autogen.sh
./configure --prefix=${root}/lib/ps/ && make && make install 

echo "##### Generating swig libraries...."
cd swig
make

echo "##### Generating jni module. Make sure you have your JAVA_HOME environment variable correctly set."
cd java
make

echo "##### Generating the maven-nar artifact."
mvn clean install

echo "##### Setting the pocketsphinx model folder into esh properties."
echo "modelpath = ${root}/pocketsphinx/pocketsphinx/model/en-us/" > "${root}/src/main/resources/pocketsphinx_en_US.properties"

echo "##### Done! Now do a full maven build of ESH."
