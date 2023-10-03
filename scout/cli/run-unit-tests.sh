#!/bin/bash
./gradlew test --stacktrace
./gradlew test -Dscout.compile.keys=true --stacktrace