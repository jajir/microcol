-- Keep DMG creation usable on CI and locked Macs without automating Finder.
-- jpackage substitutes both paths before running this script.
do shell script ("/bin/ln -s " & quoted form of "DEPLOY_INSTALL_LOCATION" & " " & quoted form of "DEPLOY_VOLUME_PATH/Applications")
