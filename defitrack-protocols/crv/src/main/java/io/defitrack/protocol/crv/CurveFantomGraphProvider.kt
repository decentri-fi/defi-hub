package io.defitrack.protocol.crv

import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class CurveFantomGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : CurvePoolGraphProvider(
    "https://api.thegraph.com/subgraphs/name/convex-community/volume-fantom", theGraphGatewayProvider, Network.FANTOM
)