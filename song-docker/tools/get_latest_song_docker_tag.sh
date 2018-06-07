#!/bin/bash

curl --silent  "https://api.github.com/repos/overture-stack/SONG/releases"  \
    | jq '.[].tag_name | match("song-docker.*") | .string' \
    | head -1 | xargs echo
