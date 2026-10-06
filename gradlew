#!/bin/sh
GRADLE_VERSION=8.10
GRADLE_DISTRIBUTION_URL=https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
GRADLE_USER_HOME=${GRADLE_USER_HOME:-$HOME/.gradle}

if [ ! -d "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
    cd "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
    curl -L -o "gradle-${GRADLE_VERSION}-bin.zip" "${GRADLE_DISTRIBUTION_URL}"
    unzip -q "gradle-${GRADLE_VERSION}-bin.zip"
    rm "gradle-${GRADLE_VERSION}-bin.zip"
fi

GRADLE_HOME=$(find "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin" -maxdepth 1 -type d -name "gradle-${GRADLE_VERSION}" | head -1)

if [ -z "$GRADLE_HOME" ]; then
    echo "Could not find Gradle installation."
    exit 1
fi

exec "${GRADLE_HOME}/bin/gradle" "$@"