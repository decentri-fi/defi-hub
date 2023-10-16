package io.defitrack.protocol.dodo

import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class DodoArbitrumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : DodoGraphProvider(
    "https://api.thegraph.com/subgraphs/name/dodoex/dodoex-v2-arbitrum",
    graphGatewayProvider
)