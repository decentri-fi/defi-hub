#!/bin/bash

docker build -t defitrack/defitrack:website-${BRANCH_NAME} defitrack/defitrack-web
docker push defitrack/defitrack:website-${BRANCH_NAME}

docker build -t defitrack/defitrack:uniswap-${BRANCH_NAME} defitrack/defitrack-uniswap
docker push defitrack/defitrack:uniswap-${BRANCH_NAME}

docker build -t defitrack/defitrack:quickswap-${BRANCH_NAME} defitrack/defitrack-quickswap
docker push defitrack/defitrack:quickswap-${BRANCH_NAME}

docker build -t defitrack/defitrack:sushiswap-${BRANCH_NAME} defitrack/defitrack-sushiswap
docker push defitrack/defitrack:sushiswap-${BRANCH_NAME}

docker build -t defitrack/defitrack:adamant-${BRANCH_NAME} defitrack/defitrack-adamant
docker push defitrack/defitrack:adamant-${BRANCH_NAME}

docker build -t defitrack/defitrack:api-gw-${BRANCH_NAME} defitrack/defitrack-api-gw
docker push defitrack/defitrack:api-gw-${BRANCH_NAME}

docker build -t defitrack/defitrack:dfyn-${BRANCH_NAME} defitrack/defitrack-dfyn
docker push defitrack/defitrack:dfyn-${BRANCH_NAME}

docker build -t defitrack/defitrack:balancer-${BRANCH_NAME} defitrack/defitrack-balancer
docker push defitrack/defitrack:balancer-${BRANCH_NAME}

docker build -t defitrack/defitrack:dmm-${BRANCH_NAME} defitrack/defitrack-dmm
docker push defitrack/defitrack:dmm-${BRANCH_NAME}

docker build -t defitrack/defitrack:mstable-${BRANCH_NAME} defitrack/defitrack-mstable
docker push defitrack/defitrack:mstable-${BRANCH_NAME}

docker build -t defitrack/defitrack:aave-${BRANCH_NAME} defitrack/defitrack-aave
docker push defitrack/defitrack:aave-${BRANCH_NAME}

docker build -t defitrack/defitrack:beefy-${BRANCH_NAME} defitrack/defitrack-beefy
docker push defitrack/defitrack:beefy-${BRANCH_NAME}

docker build -t defitrack/defitrack:curve-${BRANCH_NAME} defitrack/defitrack-curve
docker push defitrack/defitrack:curve-${BRANCH_NAME}

docker build -t defitrack/defitrack:dracula-${BRANCH_NAME} defitrack/defitrack-dracula
docker push defitrack/defitrack:dracula-${BRANCH_NAME}

docker build -t defitrack/defitrack:belt-${BRANCH_NAME} defitrack/defitrack-belt
docker push defitrack/defitrack:belt-${BRANCH_NAME}

docker build -t defitrack/defitrack:iron-${BRANCH_NAME} defitrack/defitrack-iron
docker push defitrack/defitrack:iron-${BRANCH_NAME}

docker build -t defitrack/defitrack:polycat-${BRANCH_NAME} defitrack/defitrack-polycat
docker push defitrack/defitrack:polycat-${BRANCH_NAME}

docker build -t defitrack/defitrack:yearn-${BRANCH_NAME} defitrack/defitrack-yearn
docker push defitrack/defitrack:yearn-${BRANCH_NAME}

docker build -t defitrack/defitrack:convex-${BRANCH_NAME} defitrack/defitrack-convex
docker push defitrack/defitrack:convex-${BRANCH_NAME}

docker build -t defitrack/defitrack:compound-${BRANCH_NAME} defitrack/defitrack-compound
docker push defitrack/defitrack:compound-${BRANCH_NAME}

docker build -t defitrack/defitrack:price-${BRANCH_NAME} defitrack/defitrack-price
docker push defitrack/defitrack:price-${BRANCH_NAME}

docker build -t defitrack/defitrack:abi-${BRANCH_NAME} defitrack/defitrack-abi
docker push defitrack/defitrack:abi-${BRANCH_NAME}

docker build -t defitrack/defitrack:erc20-${BRANCH_NAME} defitrack/defitrack-erc20
docker push defitrack/defitrack:erc20-${BRANCH_NAME}