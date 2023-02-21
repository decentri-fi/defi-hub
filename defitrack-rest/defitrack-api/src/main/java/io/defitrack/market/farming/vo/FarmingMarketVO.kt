package io.defitrack.market.farming.vo

import io.defitrack.market.MarketVO
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.toVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal

class FarmingMarketVO(
    id: String,
    network: NetworkVO,
    protocol: ProtocolVO,
    name: String,
    val stakedToken: FungibleToken,
    val reward: List<FungibleToken>,
    val vaultType: String,
    marketSize: BigDecimal?,
    val apr: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    val farmType: ContractType,
    val rewardsFinished: Boolean
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, marketSize, "farming"
) {
    companion object {
        fun FarmingMarket.toVO() = FarmingMarketVO(
            id = this.id,
            network = this.network.toVO(),
            protocol = this.protocol.toVO(),
            name = this.name,
            stakedToken = this.stakedToken,
            reward = this.rewardTokens,
            vaultType = this.contractType,
            marketSize = this.marketSize,
            apr = this.apr,
            prepareInvestmentSupported = this.investmentPreparer != null,
            farmType = farmType,
            rewardsFinished = this.rewardsFinished
        )
    }
}
