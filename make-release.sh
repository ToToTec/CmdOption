#!/bin/sh

VERSION="0.0.99-SNAPSHOT"

SRC="cmdoption-src-${VERSION}"
DIST="cmdoption-dist-${VERSION}"

echo "Delete target? "
read

rm -rf -- target
mkdir -p target
mkdir -p "target/${DIST}"

##############
# Source Zip #
##############


# extract sources
svn export . "target/${SRC}"

# cleanup unnecessary files
rm -f -- "target/${SRC}/make-release.sh"

# zip
( cd target && zip -r "${DIST}/${SRC}.zip" "${SRC}" )
rm -rf -- "target/${SRC}"

########
# jars #
########

PROJ="de.tototec.cmdoption"

# build
( cd "${PROJ}" && cmvn clean source:jar javadoc:jar install )

# copy jars
cp "${PROJ}/target/${PROJ}-${VERSION}.jar" "target/${DIST}"
cp "${PROJ}/target/${PROJ}-${VERSION}-sources.jar" "target/${DIST}"
cp "${PROJ}/target/${PROJ}-${VERSION}-javadoc.jar" "target/${DIST}"

########
# dist #
########


( cd target && zip -r "${DIST}.zip" "${DIST}" )

echo
echo "Finished:"
echo
find target
