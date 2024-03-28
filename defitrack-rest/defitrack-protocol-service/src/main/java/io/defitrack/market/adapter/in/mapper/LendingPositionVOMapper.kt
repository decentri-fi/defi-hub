package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.toVO
import io.defitrack.market.domain.lending.LendingPosition
import io.defitrack.market.adapter.`in`.resource.LendingPositionVO
import io.defitrack.network.toVO
import io.defitrack.port.output.PriceClient
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class LendingPositionVOMapper(
    private val protocolVOMapper: ProtocolVOMapper,
    private val lendingMarketVOMapper: LendingMarketVOMapper,
    private val priceResource: PriceClient,
) {

    suspend fun map(lendingPosition: LendingPosition): LendingPositionVO {
        return with(lendingPosition) {

            val lendingInDollars = if (market.marketToken !== null && market.price.get() > BigDecimal.ZERO) {
                tokenAmount.asEth(market.marketToken!!.decimals).times(market.price.get())
            } else {
                priceResource.calculatePrice(
                    GetPriceCommand(
                        address = market.token.address,
                        network = market.network,
                        amount = underlyingAmount.asEth(market.token.decimals),
                    )
                ).toBigDecimal()
            }

            LendingPositionVO(
                network = market.network.toVO(),
                protocol = protocolVOMapper.map(market.protocol),
                dollarValue = lendingInDollars,
                rate = market.rate?.toDouble(),
                name = market.name,
                amountDecimal = underlyingAmount.asEth(market.token.decimals).toDouble(),
                id = market.id,
                token = market.token.toVO(),
                exitPositionSupported = market.exitPositionPreparer !== null,
                amount = tokenAmount,
                market = lendingMarketVOMapper.map(market)
            )
        }
    }
}