package io.defitrack.protocol.application.pendle

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pendle.PendleMarketContract
import io.defitrack.protocol.pendle.PendleMarketFactoryContract
import io.defitrack.protocol.pendle.PendleSyContract
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@ConditionalOnNetwork(Network.ARBITRUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleArbitrumiquidityPoolMarketProvider : PendleLiquidityPoolMarketProvider(
    "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced", "154873897", Network.ARBITRUM
)