package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.adapter.`in`.resource.FarmingMarketVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class FarmingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) : MarketVOMapper<FarmingMarket> {

    override suspend fun map(market: FarmingMarket): FarmingMarketVO {
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