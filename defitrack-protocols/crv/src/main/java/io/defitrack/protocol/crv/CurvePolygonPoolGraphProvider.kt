package io.defitrack.protocol.crv

import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class CurvePolygonPoolGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : CurvePoolGraphProvider(
    "https://api.thegraph.com/subgraphs/name/convex-community/volume-matic", theGraphGatewayProvider, Network.ETHEREUM
)