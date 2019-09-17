#!/bin/bash

cd `dirname $0`

EXOPLAYER_ROOT="$(pwd)"
FLAC_EXT_PATH="${EXOPLAYER_ROOT}/extensions/flac/src/main"
NDK_PATH=/mnt/files/android/android-ndk-r17c

cd "${FLAC_EXT_PATH}/jni" 
if [ ! -d flac ]; then
	curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-1.3.3.tar.xz | tar xJ && \
	mv flac-1.3.3 flac
fi

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







