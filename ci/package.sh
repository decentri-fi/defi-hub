#!/usr/bin/bash

function startNetwork {
  # docker build -t ${DOCKER_REPO}:$1-${BRANCH_NAME} defitrack-rest/defitrack-blockchain-services/defitrack-$1 -f ci/Dockerfile
  ./mvnw -pl defitrack-rest/defitrack-blockchain-services/defitrack-$1 spring-boot:build-image

}

function startProtocol {
  ./mvnw -pl defitrack-rest/defitrack-protocol-services/defitrack-$1 spring-boot:build-image
  #docker build -t ${DOCKER_REPO}:$1-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-$1 -f ci/Dockerfile
}


function startInfra {
  # docker build -t ${DOCKER_REPO}:$1-${BRANCH_NAME} defitrack-rest/defitrack-$1 -f ci/Dockerfile
  ./mvnw -pl defitrack-rest/defitrack-$1 spring-boot:build-image
}

git clone https://github.com/decentri-fi/infrastructure

for package in $(cat infrastructure/networks.txt); do startNetwork $package; done
for package in $(cat infrastructure/protocols.txt); do startProtocol $package; done
for package in $(cat infrastructure/infra.txt); do startInfra $package; done

mvn -pl defitrack-rest/defitrack-balance spring-boot:build-image