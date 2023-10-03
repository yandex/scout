#!/bin/sh

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)"

./release-base.sh
./release-compiled.sh
./release-plugin.sh