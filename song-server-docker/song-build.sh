#!/bin/bash
VERSION=1.4.1
docker build --build-arg SONG_VERSION=${VERSION} --build-arg SONG_ARTIFACTORY_REPO_NAME=dcc-release -f Dockerfile.song -t overture/song:${VERSION} ./
