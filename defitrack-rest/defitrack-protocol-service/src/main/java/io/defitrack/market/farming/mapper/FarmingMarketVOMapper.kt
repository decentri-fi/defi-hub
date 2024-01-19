package io.defitrack.market.farming.mapper

import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.MarketVOMapper
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class FarmingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) : MarketVOMapper<FarmingMarket> {

    override fun map(market: FarmingMarket): FarmingMarketVO {
        return with(market) {
            FarmingMarketVO(
                id = this.id,
                network = this.network.toNetworkInformation(),
                protocol = protocolVOMapper.map(this.protocol),
                name = this.name,
                stakedToken = this.stakedToken,
                reward = this.rewardTokens,
                marketSize = this.marketSize?.get(),
                apr = this.apr,
                prepareInvestmentSupported = this.investmentPreparer != null,
                exitPositionSupported = this.exitPositionPreparer != null,
                deprecated = this.deprecated,
                updatedAt = Date.from(updatedAt.get().toInstant(ZoneOffset.UTC)),
                token = this.token
            )
        }
    }

}