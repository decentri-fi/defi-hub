package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.market.domain.lending.LendingPosition
import io.defitrack.market.adapter.`in`.resource.LendingPositionVO
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class LendingPositionVOMapper(
    private val protocolVOMapper: ProtocolVOMapper,
    private val lendingMarketVOMapper: LendingMarketVOMapper,
    private val priceResource: PricePort,
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
                network = market.network.toNetworkInformation(),
                protocol = protocolVOMapper.map(market.protocol),
                dollarValue = lendingInDollars,
                rate = market.rate?.toDouble(),
                name = market.name,
                amountDecimal = underlyingAmount.asEth(market.token.decimals).toDouble(),
                id = market.id,
                token = market.token,
                exitPositionSupported = market.exitPositionPreparer !== null,
                amount = tokenAmount,
                market = lendingMarketVOMapper.map(market)
            )
        }
    }
}