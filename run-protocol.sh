#!/bin/sh

export SPRING_PROFILES_ACTIVE=local
./mvnw -pl defitrack-rest/defitrack-protocol-services/defitrack-$1 -am package
java -jar defitrack-rest/defitrack-protocol-services/defitrack-$1/target/defitrack-$1-0.0.1-SNAPSHOT.jar