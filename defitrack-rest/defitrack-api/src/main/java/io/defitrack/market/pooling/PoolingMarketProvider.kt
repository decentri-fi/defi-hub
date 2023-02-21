package io.defitrack.market.pooling

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.invest.MarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare

abstract class PoolingMarketProvider : MarketProvider<PoolingMarket>() {
    suspend fun defaultBreakdown(
        tokens: List<TokenInformationVO>,
        poolAddress: String
    ): List<PoolingMarketTokenShare>? {
        return tokens.map {
            PoolingMarketTokenShare(
                token = it.toFungibleToken(),
                reserve = getBalance(it.address, poolAddress),
                reserveUSD = getMarketSize(it.toFungibleToken(), poolAddress)
            )
        }
    }
}