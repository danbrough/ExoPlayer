#!/bin/bash
cd `dirname $0`

FFMPEG_EXT_PATH="$(pwd)/src/main"
NDK_PATH="/opt/sdk/android/ndk-bundle"
HOST_PLATFORM="linux-x86_64"
cd "${FFMPEG_EXT_PATH}/jni" && \
git clone git://source.ffmpeg.org/ffmpeg ffmpeg
cd "${FFMPEG_EXT_PATH}/jni/ffmpeg" && \
git checkout release/4.2
ENABLED_DECODERS=(vorbis opus flac)

cd "${FFMPEG_EXT_PATH}/jni" && \
./build_ffmpeg.sh \
  "${FFMPEG_EXT_PATH}" "${NDK_PATH}" "${HOST_PLATFORM}" "${ENABLED_DECODERS[@]}"

#./build_ffmpeg.sh .. /opt/sdk/android/ndk-bundle linux-x86_64
