package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.adapter.`in`.resource.BorrowPositionVO
import io.defitrack.market.domain.borrow.BorrowPosition
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.`in`.PricePort
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class BorrowingPositionVOMapper(
    private val pricePort: PricePort,
    private val protocolVOMapper: ProtocolVOMapper
) {

    suspend fun toVO(borrowPosition: BorrowPosition): BorrowPositionVO {
        return with(borrowPosition) {
            BorrowPositionVO(
                network = market.network.toNetworkInformation(),
                dollarValue = pricePort.calculatePrice(
                    GetPriceCommand(
                        market.token.address,
                        market.network,
                        underlyingAmount.asEth(market.token.decimals),
                    )
                ),
                protocol = protocolVOMapper.map(market.protocol),
                rate = market.rate,
                name = market.name,
                amount = tokenAmount.asEth(market.token.decimals).toDouble(),
                underlyingAmount = underlyingAmount.asEth(market.token.decimals).toDouble(),
                id = market.id,
                token = market.token
            )
        }
    }

}