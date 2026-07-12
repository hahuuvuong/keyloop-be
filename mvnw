#!/bin/sh
set -e
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
if [ ! -f "$JAR" ]; then
  curl -fsSL https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar -o "$JAR"
fi
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -classpath "$JAR" -Dmaven.multiModuleProjectDirectory="$BASE_DIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
