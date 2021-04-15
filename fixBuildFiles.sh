#!/bin/bash
cd `dirname $0`

find -type f -name build.gradle | while read n; do
echo processing $n
sed  -e 's:testOptions.unitTests.includeAndroidResources = true:testOptions.unitTests.includeAndroidResources = true\ntask sourcesJar(type\:Jar) {\nfrom android.sourceSets.main.java.srcDirs\n classifier = '\''sources'\''\n}\nartifacts { archives sourcesJar }\n:g'  -i "$n"
done


