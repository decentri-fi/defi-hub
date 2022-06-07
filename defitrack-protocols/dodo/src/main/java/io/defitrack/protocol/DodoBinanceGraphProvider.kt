package io.defitrack.protocol

import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class DodoBinanceGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : DodoGraphProvider(
    "https://api.thegraph.com/subgraphs/name/stan36/messari-dodov2-bsc",
    graphGatewayProvider
)