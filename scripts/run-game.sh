#!/bin/bash
# Select the same Java major version used by CI and packaged runtimes.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ -z "${JAVA_HOME:-}" && "$(uname -s)" == Darwin ]]; then
    if command -v brew >/dev/null 2>&1 && brew --prefix openjdk@25 >/dev/null 2>&1; then
        JAVA_HOME="$(brew --prefix openjdk@25)/libexec/openjdk.jdk/Contents/Home"
    else
        JAVA_HOME=$(/usr/libexec/java_home -v 25)
    fi
    export JAVA_HOME
fi
if [[ -n "${JAVA_HOME:-}" ]]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# Validation rejects another JDK before JavaFX starts; its executable is Maven's java.home.
exec mvn -pl akropia-game validate compile javafx:run "$@"
