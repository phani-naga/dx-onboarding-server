#!/bin/bash

MAJOR_RELEASE=5
MINOR_RELEASE=0
PATCH_RELEASE=0-alpha
COMMIT_ID=`git log -1 --pretty=%h` # last commit-id in short form

# To be executed from project root
docker build -t ghcr.io/datakaveri/onboarding-server-depl:$MAJOR_RELEASE.$MINOR_RELEASE.$PATCH_RELEASE-$COMMIT_ID -f docker/depl.dockerfile . && \
docker push ghcr.io/datakaveri/onboarding-server-depl:$MAJOR_RELEASE.$MINOR_RELEASE.$PATCH_RELEASE-$COMMIT_ID
