#!/bin/bash
# Install only into the user's data directory; game saves stay in ~/.akropia.
set -eu
source_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
data_dir=${XDG_DATA_HOME:-$HOME/.local/share}
case "$data_dir" in /*) ;; *) printf 'XDG_DATA_HOME must be absolute.\n' >&2; exit 1 ;; esac
root=${AKROPIA_DEV_ROOT:-$data_dir/akropia-dev}
case "$root" in /*) ;; *) printf 'AKROPIA_DEV_ROOT must be absolute.\n' >&2; exit 1 ;; esac
mkdir -p "$root/launcher" "$data_dir/applications"
temporary=$(mktemp "$root/launcher/install.XXXXXXXX")
trap 'rm -f -- "$temporary"' EXIT
cp "$source_dir/akropia-dev" "$temporary"
chmod 755 "$temporary"
mv -f -- "$temporary" "$root/launcher/akropia-dev"
cp "$source_dir/Akropia.png" "$root/launcher/Akropia.png"
# Escape according to Desktop Entry's quoted Exec value rules, including field codes.
launcher="$root/launcher/akropia-dev"
launcher=${launcher//\\/\\\\}
launcher=${launcher//\"/\\\"}
launcher=${launcher//\$/\\\$}
launcher=${launcher//\`/\\\`}
launcher=${launcher//%/%%}
cat > "$data_dir/applications/akropia-dev.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=Akropia Dev
Comment=Update and play the current Akropia development build
Exec="$launcher"
Icon=$root/launcher/Akropia.png
Terminal=true
Categories=Game;StrategyGame;
StartupNotify=false
EOF
printf 'Installed Akropia Dev in %s\nLaunch it from the application menu, or run:\n  %s\n' \
    "$root/launcher" "$root/launcher/akropia-dev"
