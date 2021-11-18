#!/bin/bash

docker build -t defitrack/defitrack:uniswap-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-uniswap
docker build -t defitrack/defitrack:quickswap-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-quickswap
docker build -t defitrack/defitrack:sushiswap-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-sushiswap
docker build -t defitrack/defitrack:adamant-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-adamant
docker build -t defitrack/defitrack:api-gw-${BRANCH_NAME} defitrack-rest/defitrack-api-gw
docker build -t defitrack/defitrack:dfyn-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-dfyn
docker build -t defitrack/defitrack:balancer-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-balancer
docker build -t defitrack/defitrack:dmm-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-dmm
docker build -t defitrack/defitrack:mstable-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-mstable
docker build -t defitrack/defitrack:aave-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-aave
docker build -t defitrack/defitrack:beefy-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-beefy
docker build -t defitrack/defitrack:curve-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-curve
docker build -t defitrack/defitrack:polycat-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-polycat
docker build -t defitrack/defitrack:convex-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-convex
docker build -t defitrack/defitrack:compound-${BRANCH_NAME} defitrack-rest/defitrack-protocol-services/defitrack-compound
docker build -t defitrack/defitrack:price-${BRANCH_NAME} defitrack-rest/defitrack-uniswap
docker build -t defitrack/defitrack:abi-${BRANCH_NAME} defitrack-rest/defitrack-abi
docker build -t defitrack/defitrack:erc20-${BRANCH_NAME} defitrack-rest/defitrack-erc20
