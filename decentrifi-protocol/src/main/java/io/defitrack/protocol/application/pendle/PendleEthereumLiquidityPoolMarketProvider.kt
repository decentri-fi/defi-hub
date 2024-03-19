package io.defitrack.protocol.application.pendle

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pendle.PendleMarketContract
import io.defitrack.protocol.pendle.PendleMarketFactoryContract
import org.springframework.stereotype.Component

@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleEthereumLiquidityPoolMarketProvider : PendleLiquidityPoolMarketProvider(
    "0x1A6fCc85557BC4fB7B534ed835a03EF056552D52", "18669498", Network.ETHEREUM

)