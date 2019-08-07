#!/bin/bash

cd `dirname $0`
cd src/main/jni

#rm -rf flac
if [ ! -d flac ]; then 
	curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-1.3.1.tar.xz | tar xJ && \
	mv flac-1.3.1 flac
fi

ndk-build APP_ABI=all -j4
