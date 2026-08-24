#!/bin/bash

set -e

./gradlew build

rm -rf build/dependency
mkdir -p build/dependency

cd build/dependency
jar -xf ../libs/memome-backend-0.0.1-SNAPSHOT.jar
cd -

echo "Build completed."