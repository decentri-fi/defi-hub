package io.defitrack.market.farming.mapper

import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class FarmingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) {

    fun map(farmingMarket: FarmingMarket): FarmingMarketVO {
        return with(farmingMarket) {
            FarmingMarketVO(
                id = this.id,
                network = this.network.toVO(),
                protocol = protocolVOMapper.map(this.protocol),
                name = this.name,
                stakedToken = this.stakedToken,
                reward = this.rewardTokens,
                vaultType = this.contractType,
                marketSize = this.marketSize,
                apr = this.apr,
                prepareInvestmentSupported = this.investmentPreparer != null,
                exitPositionSupported = this.exitPositionPreparer != null,
                farmType = farmType,
                expired = this.expired
            )
        }
    }

}