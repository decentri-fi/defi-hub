package io.defitrack.market.pooling.mapper

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.pooling.PoolingPosition
import io.defitrack.market.pooling.vo.PoolingPositionVO
import io.defitrack.network.toVO
import io.defitrack.port.output.ERC20Client
import io.defitrack.port.output.PriceClient
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class PoolingPositionVOMapper(
    private val erC20Resource: ERC20Client,
    private val protocolVOMapper: ProtocolVOMapper,
    private val breakdownMapper: PoolingBreakdownVOMapper,
    private val poolingMarketVOMapper: PoolingMarketVOMapper,
    private val prices: PriceClient
) {

    suspend fun map(poolingPosition: PoolingPosition): PoolingPositionVO {
        return with(poolingPosition) {
            val lpToken = erC20Resource.getTokenInformation(market.network, market.address)
            val amount = tokenAmount.asEth(lpToken.decimals)

            val dollarValue = customPriceCalculator?.calculate() ?: run {
                prices.calculatePrice(
                    GetPriceCommand(
                        lpToken.address,
                        poolingPosition.market.network,
                        amount
                    )
                )
            }

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