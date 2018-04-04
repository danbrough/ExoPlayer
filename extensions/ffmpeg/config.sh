#!/usr/bin/env bash

clean_ffmpeg(){
     git reset --hard && git checkout n3.4.2
}


NDK_PATH=/mnt/files/android/ndk
HOST_PLATFORM="linux-x86_64"
COMMON_OPTIONS="\
    --target-os=android \
    --disable-static \
    --enable-shared \
    --disable-doc \
    --disable-programs \
    --disable-everything \
    --disable-avdevice \
    --disable-avformat \
    --disable-swscale \
    --disable-postproc \
    --disable-avfilter \
    --disable-symver \
    --disable-swresample \
    --enable-avresample \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac \
    --enable-decoder=mp3 \
    --enable-decoder=eac3 \
    --enable-decoder=ac3 \
    --enable-decoder=aac \
    --enable-decoder=opus "


