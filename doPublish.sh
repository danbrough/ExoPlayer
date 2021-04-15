#!/bin/bash

cd `dirname $0`

TASKS=""

EXTNS="flac opus ffmpeg cast media2"
MODULES="core common smoothstreaming ui hls extractor dash"
ACTIONS="publishMavenAarPublicationToMavenLocal"

for action in $ACTIONS; do
for extn in  $EXTNS; do
	TASKS="$TASKS  :extension-$extn:$action "
done
for mod in $MODULES; do
	TASKS="$TASKS :library-$mod:$action"
done
done

gradle $TASKS



rsync -avHSx /home/dan/.m2/repository/com/github/danbrough/ h1:/srv/https/maven/com/github/danbrough/
