package io.defitrack.market.pooling.domain

import io.defitrack.common.network.Network
import io.defitrack.market.DefiMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.market.lending.domain.BalanceFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigDecimal

data class PoolingMarket(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val symbol: String,
    val tokens: List<FungibleToken>,
    val apr: BigDecimal? = null,
    val marketSize: BigDecimal? = null,
    val tokenType: TokenType,
    val balanceFetcher: BalanceFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null
) : DefiMarket