package io.defitrack.market.lending.mapper

import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.MarketVOMapper
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.vo.LendingMarketVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class LendingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) : MarketVOMapper<LendingMarket> {

    override fun map(market: LendingMarket): LendingMarketVO {
        return with(market) {
            LendingMarketVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toNetworkInformation(),
                token = token,
                marketToken = marketToken,
                rate = rate?.toDouble(),
                poolType = poolType,
                marketSize = marketSize?.get(),
                prepareInvestmentSupported = investmentPreparer != null,
                exitPositionSupported = this.exitPositionPreparer != null,
                erc20Compatible = this.erc20Compatible,
                price = price.get(),
                totalSupply = totalSupply.get(),
                updatedAt = Date.from(updatedAt.get().toInstant(ZoneOffset.UTC)).time,
                deprecated = deprecated
            )
        }
    }
}