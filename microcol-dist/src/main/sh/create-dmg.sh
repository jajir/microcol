#!/bin/bash

#
# Move to the directory containing this script so we can source the env.sh
# properties that follow
#
cd `dirname $0`

JPACKAGE_HOME=/Users/jan/Documents/jdk-14.jdk/Contents/Home

target="../../../target"
src="../../../src"

cmd="$JPACKAGE_HOME/bin/jpackage --type dmg \
                --name MicroCol \
                --app-version 0.7.0 \
                --icon $src/icon.icns \
                --module microcol.game/org.microcol.MicroCol \
                --runtime-image $target/dist/ \
                --dest $target/"
echo $cmd

${cmd}