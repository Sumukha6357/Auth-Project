#!/bin/sh
set -eu

exec java ${JAVA_OPTS:-} -jar /app/idp.jar
