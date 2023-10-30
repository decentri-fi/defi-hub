package io.defitrack.market.pooling.mapper

import io.defitrack.market.MarketVOMapper
import io.defitrack.market.pooling.breakdown.PoolingBreakdownMapper
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class PoolingMarketVOMapper(
    private val poolingBreakdownMapper: PoolingBreakdownMapper,
    private val protocolVOMapper: ProtocolVOMapper
) : MarketVOMapper<PoolingMarket> {

    override fun map(market: PoolingMarket): PoolingMarketVO {
        return with(market) {
            PoolingMarketVO(
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                tokens = tokens,
                id = id,
                breakdown = poolingBreakdownMapper.toVO(breakdown),
                decimals = decimals,
                address = address,
                apr = apr,
                marketSize = marketSize?.get(),
                prepareInvestmentSupported = investmentPreparer != null,
                erc20Compatible = erc20Compatible,
                exitPositionSupported = exitPositionPreparer != null,
                price = price.get(),
                totalSupply = totalSupply.get(),
                metadata = metadata,
                updatedAt = Date.from(updatedAt.get().toInstant(ZoneOffset.UTC)).time,
                deprecated = deprecated,
                historySupported = historicEventExtractor != null
            )
        }
    }
}