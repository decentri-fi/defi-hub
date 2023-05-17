package io.defitrack.market.pooling.mapper

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.breakdown.PoolingBreakdownMapper
import io.defitrack.market.pooling.domain.PoolingPosition
import io.defitrack.market.pooling.vo.PoolingPositionVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class PoolingPositionVOMapper(
    private val erC20Resource: ERC20Resource,
    private val protocolVOMapper: ProtocolVOMapper,
    private val breakdownMapper: PoolingBreakdownMapper,
    private val poolingMarketVOMapper: PoolingMarketVOMapper
) {

    suspend fun map(poolingPosition: PoolingPosition): PoolingPositionVO {
        return with(poolingPosition) {
            val lpToken = erC20Resource.getTokenInformation(market.network, market.address)
            val amount = tokenAmount.asEth(lpToken.decimals)
            val dollarValue = customPriceCalculator?.calculate() ?: amount.times(market.price.get()).toDouble()

            PoolingPositionVO(
                lpAddress = market.address,
                amountDecimal = amount,
                name = market.name,
                dollarValue = dollarValue,
                network = market.network.toVO(),
                symbol = market.symbol,
                protocol = protocolVOMapper.map(market.protocol),
                id = market.id,
                exitPositionSupported = market.exitPositionPreparer != null,
                amount = tokenAmount,
                breakdown = breakdownMapper.toPositionVO(market, amount),
                market = poolingMarketVOMapper.map(market)
            )
        }
    }
}