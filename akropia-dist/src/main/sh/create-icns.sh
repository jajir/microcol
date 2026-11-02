#!/bin/bash
#
# Generate ICNS apple icon.
#

#
# Move to base maven project directory. 
#
cd `dirname $0/`
cd ../../../

dir=target/icon.iconset

mkdir $dir

cp src/icon.png target/icon.iconset/

sips -z 32 32     $dir/icon.png --out $dir/icon_16x16@2x.png
sips -z 32 32     $dir/icon.png --out $dir/icon_32x32.png
sips -z 64 64     $dir/icon.png --out $dir/icon_32x32@2x.png
sips -z 128 128   $dir/icon.png --out $dir/icon_128x128.png
sips -z 256 256   $dir/icon.png --out $dir/icon_128x128@2x.png
sips -z 256 256   $dir/icon.png --out $dir/icon_256x256.png
sips -z 512 512   $dir/icon.png --out $dir/icon_256x256@2x.png
sips -z 512 512   $dir/icon.png --out $dir/icon_512x512.png

cp $dir/icon.png $dir/icon_512x512@2x.png

# remove the base image
rm -rf $dir/icon.png

# create the .icns
iconutil --convert icns $dir

# remove the temp folder
rm -R $dir