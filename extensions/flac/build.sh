#!/bin/bash

cd `dirname $0`
cd src/main/jni

rm -rf flac
curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-1.3.1.tar.xz | tar xJ && \
mv flac-1.3.1 flac
ndk-build APP_ABI=all -j4