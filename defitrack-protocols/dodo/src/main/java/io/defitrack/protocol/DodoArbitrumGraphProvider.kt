package io.defitrack.protocol

import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class DodoArbitrumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : DodoGraphProvider(
    "https://api.thegraph.com/subgraphs/name/dodoex/dodoex-v2-arbitrum",
    graphGatewayProvider
)