#!/bin/bash

cd `dirname $0`

EXOPLAYER_ROOT="$(pwd)"
FLAC_EXT_PATH="${EXOPLAYER_ROOT}/extensions/flac/src/main"
NDK_PATH=/opt/sdk/android/ndk-bundle
FLAC_VERSION=1.3.3

cd "${FLAC_EXT_PATH}/jni" 
if [ ! -d flac ]; then
	git clone git@github.com:xiph/flac.git flac 
	#curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-$FLAC_VERSION.tar.xz | tar xJ && \
	#mv flac-$FLAC_VERSION flac
fi
cd flac && git clean -xdf && git reset --hard && git checkout $FLAC_VERSION && cd ..

rm -rf "$FLAC_EXT_PATH/obj" "$FLAC_EXT_PATH/libs"
cd "${FLAC_EXT_PATH}"/jni && \
${NDK_PATH}/ndk-build APP_ABI=all -j4

cd "$EXO_PLAYER_ROOT" 
OPUS_EXT_PATH="${EXOPLAYER_ROOT}/extensions/opus/src/main"

cd "${OPUS_EXT_PATH}/jni" 
if [ ! -d libopus ]; then
	git clone https://git.xiph.org/opus.git libopus
else
	cd libopus && git clean -xdf && git reset --hard && git pull 
fi

rm -rf "$OPUS_EXT_PATH/obj" "$OPUS_EXT_PATH/libs"
cd "${OPUS_EXT_PATH}"/jni 
./convert_android_asm.sh

${NDK_PATH}/ndk-build APP_ABI=all -j4







