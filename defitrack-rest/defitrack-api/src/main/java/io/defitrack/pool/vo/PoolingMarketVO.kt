package io.defitrack.pool.vo

import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.toVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

data class PoolingMarketVO(
    val id: String,
    val address: String,
    val name: String,
    val protocol: ProtocolVO,
    val network: NetworkVO,
    val tokens: List<FungibleToken>,
    val apr: BigDecimal?,
    val marketSize: BigDecimal?,
    val prepareInvestmentSupported: Boolean
) {
    companion object {
        fun PoolingMarketElement.toVO() =
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