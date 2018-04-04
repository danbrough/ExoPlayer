#!/bin/bash



cd `dirname $0`
source config.sh

cd src/main/jni




if [ ! -d ffmpeg ]; then
    git clone git://source.ffmpeg.org/ffmpeg ffmpeg
fi
cd ffmpeg && clean_ffmpeg

echo '########################### building armeabi-v7a'
./configure \
    --libdir=android-libs/armeabi-v7a \
    --arch=arm \
    --cpu=armv7-a \
    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/${HOST_PLATFORM}/bin/arm-linux-androideabi-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
    --extra-cflags="-march=armv7-a -mfloat-abi=softfp  " \
    --extra-ldflags="-Wl,--fix-cortex-a8" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} || (cat config.log && exit 1)

make clean && make -j4 && make install-libs || exit 1

echo '########################### building arm64-v8a'
clean_ffmpeg && ./configure \
    --libdir=android-libs/arm64-v8a \
    --arch=aarch64 \
    --cpu=armv8-a \
    --cross-prefix="${NDK_PATH}/toolchains/aarch64-linux-android-4.9/prebuilt/${HOST_PLATFORM}/bin/aarch64-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-21/arch-arm64/" \
    --extra-ldexeflags=-pie \
    ${COMMON_OPTIONS} || (cat config.log && exit 1)

make clean && make -j4 && make install-libs || exit 1

echo '########################### building x86'

clean_ffmpeg && ./configure \
    --libdir=android-libs/x86 \
    --arch=x86 \
    --cpu=i686 \
    --cross-prefix="${NDK_PATH}/toolchains/x86-4.9/prebuilt/${HOST_PLATFORM}/bin/i686-linux-android-" \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-x86/" \
    --extra-ldexeflags=-pie \
    --disable-asm \
    ${COMMON_OPTIONS} || (cat config.log && exit 1)

make clean && make -j4 && make install-libs || exit 1



