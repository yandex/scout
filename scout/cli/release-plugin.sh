#!/bin/sh

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)"

cd ../tools/scout-gradle-plugin
./../../gradlew publishAllPublicationsToStagingRepository --max-workers 1