package io.defitrack.market.pooling.mapper

import io.defitrack.market.adapter.`in`.mapper.MarketVOMapper
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.util.*

@Component
class PoolingMarketVOMapper(
    private val poolingBreakdownVOMapper: PoolingBreakdownVOMapper,
    private val protocolVOMapper: ProtocolVOMapper,
) : MarketVOMapper<PoolingMarket> {

    override suspend fun map(market: PoolingMarket): PoolingMarketVO {
        return with(market) {
            PoolingMarketVO(
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                id = id,
                breakdown = poolingBreakdownVOMapper.toVO(breakdown.get()),
                decimals = decimals,
                address = address,
                apr = apr,
                prepareInvestmentSupported = investmentPreparer != null,
                erc20Compatible = erc20Compatible,
                exitPositionSupported = exitPositionPreparer != null,
                totalSupply = totalSupply.get(),
                metadata = metadata,
                updatedAt = Date.from(updatedAt.get().toInstant(ZoneOffset.UTC)).time,
                deprecated = deprecated
            )
        }
    }
}