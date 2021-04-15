#!/bin/bash
cd `dirname $0`

if [ ! -f .version ]; then
	echo 0 > .version
fi
VERSION=`cat .version`
VERSION=$(( $VERSION + 1 ))
echo VERSION $VERSION
echo $VERSION > .version

VERSION_NAME=`printf '2.13.3-dan%02d'  $VERSION`
echo VERSION_NAME $VERSION_NAME
sed -i  constants.gradle -e 's:releaseVersion =.*:releaseVersion = "'$VERSION_NAME'":g'
git commit -am "$VERSION_NAME"
git tag -d "$VERSION_NAME"
git tag "$VERSION_NAME"

git push origin 
git push origin --delete "$VERSION_NAME"
git push origin "$VERSION_NAME"

curl https://jitpack.io/com/github/danbrough/ExoPlayer/${VERSION_NAME}/build.log

./gradlew publishToMavenLocal || exit 1
find ~/.m2/repository/com/github/danbrough/exoplayer/ -type f -name 'maven-metadata-local.xml'  | \
	while read n; do mv "$n"  "$(echo $n | sed  -e 's:-local::g')"; done

rsync -avHSx /home/dan/.m2/repository/com/github/danbrough/ h1:/srv/https/maven/com/github/danbrough/
