#!/bin/sh
MODULE_NAME=sparqled_0.9-4
echo "Building Debian package for ${MODULE_NAME}"
echo
rm -rf ../target/deb-pkg
mkdir -p ../target/deb-pkg
# Add the Debian control files
cp -r debian ../target/deb-pkg
cp ../recommendation-servlet/target/sparqled.war ../target/deb-pkg
cp data/* ../target/deb-pkg
# Build the package and sign it.
cd ../target/deb-pkg
debuild --check-dirname-level 0 -b
