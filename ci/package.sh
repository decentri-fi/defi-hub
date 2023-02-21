#!/usr/bin/bash

function startNetwork {
  docker build -t defitrack/defitrack:$1-${BRANCH_NAME} defitrack-rest/defitrack-blockchain-services/defitrack-$1 -f ci/Dockerfile
}

function startProtocol {
  docker build -t defitrack/defitrack:$1-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-$1 -f ci/Dockerfile
}


function startInfra {
  docker build -t defitrack/defitrack:$1-${BRANCH_NAME} defitrack-rest/defitrack-$1 -f ci/Dockerfile
}


for package in $(cat ci/networks.txt); do startNetwork $package; done
for package in $(cat ci/protocols.txt); do startProtocol $package; done
for package in $(cat ci/infra.txt); do startInfra $package; done