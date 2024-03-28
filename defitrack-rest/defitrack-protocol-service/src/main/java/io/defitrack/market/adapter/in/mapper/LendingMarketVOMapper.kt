package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.erc20.toVO
import io.defitrack.market.adapter.`in`.resource.LendingMarketVO
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class LendingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) : MarketVOMapper<LendingMarket> {

    override suspend fun map(market: LendingMarket): LendingMarketVO {
        return with(market) {
            LendingMarketVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                token = token.toVO(),
                marketToken = marketToken?.toVO(),
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