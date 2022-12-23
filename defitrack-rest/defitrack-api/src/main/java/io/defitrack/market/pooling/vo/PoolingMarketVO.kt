package io.defitrack.market.pooling.vo

import io.defitrack.market.MarketVO
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.toVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

class PoolingMarketVO(
    id: String,
    address: String,
    name: String,
    protocol: ProtocolVO,
    network: NetworkVO,
    val tokens: List<FungibleToken>,
    val apr: BigDecimal?,
    marketSize: BigDecimal?,
    prepareInvestmentSupported: Boolean
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, marketSize, "pooling"
) {
    companion object {
        fun PoolingMarket.toVO() =
            PoolingMarketVO(
                name = name,
                protocol = protocol.toVO(),
                network = network.toVO(),
                tokens = tokens,
                id = id,
                address = address,
                apr = apr,
                marketSize = marketSize,
                prepareInvestmentSupported = investmentPreparer != null
            )
    }
}