#!/bin/bash
cd `dirname $0`

git checkout release-v2 || exit 1
git pull
git checkout master 
git merge release-v2

