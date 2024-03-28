package io.defitrack.protocol.application.pendle

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@ConditionalOnNetwork(Network.ARBITRUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleArbitrumiquidityPoolMarketProvider : PendleLiquidityPoolMarketProvider(
    "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced", "154873897", Network.ARBITRUM
)