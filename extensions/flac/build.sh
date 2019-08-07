#!/bin/bash

cd `dirname $0`
cd src/main/jni

FLAC_VERSION=1.3.2
rm -rf flac
if [ ! -d flac ]; then 
	curl https://ftp.osuosl.org/pub/xiph/releases/flac/flac-${FLAC_VERSION}.tar.xz | tar xJ && \
	mv flac-${FLAC_VERSION} flac
fi

ndk-build APP_ABI=all -j4
