#!/bin/bash
cd `dirname $0`

if [ ! -f .version ]; then
	echo 0 > .version
fi
VERSION=`cat .version`
VERSION=$(( $VERSION + 1 ))
echo VERSION $VERSION
echo $VERSION > .version

VERSION_NAME=`printf 'r2.11.4-dan%02d'  $VERSION`
echo VERSION_NAME $VERSION_NAME

git commit -am "$VERSION_NAME"
git tag "$VERSION_NAME"
git push origin && git push origin "$VERSION_NAME"

curl https://jitpack.io/com/github/danbrough/ExoPlayer/${VERSION_NAME}/build.log
