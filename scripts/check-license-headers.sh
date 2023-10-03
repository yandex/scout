#!/bin/bash

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)"
repository_root="$(dirname "$script_dir")"

correct=false

while :; do
    case $1 in
        -c|--correct)
        correct=true
        ;;
        *) break
    esac
    shift
done

license=$(cat "${script_dir}/license_header")

fails=0
for file in $(find $repository_root -name '*.kt');
do
    header=$(head -15 "$file")
    if [ "$header" != "$license" ];
    then
        if [ "$correct" = true ]; then
            echo "${license}
$(cat "$file")" > "$file"
            echo "Note: Appended license header to file ${file}"
        else
            let fails=fails+1
            >&2 echo "Error: Missing license header in file ${file}"
        fi
    fi
done

if [ "$fails" -gt 0 ]; then
    >&2 echo "Failure: Check failed with ${fails} errors. Please fix problems and try again. You also can re-run this script with --correct argument to append headers automatically."
    exit 1
fi