package io.defitrack.market.lending

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.domain.GetPriceCommand
import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.borrowing.BorrowPositionProvider
import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.market.borrowing.vo.BorrowPositionVO
import io.defitrack.port.input.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/borrowing")
class DefaultBorrowingRestController(
    private val borrowingServices: List<BorrowPositionProvider>,
    private val priceResource: PriceResource,
    private val protocolVOMapper: ProtocolVOMapper
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    suspend fun getPoolingMarkets(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<BorrowPositionVO> {
        return borrowingServices
            .filter {
                it.getProtocol().slug == protocol
            }
            .flatMap {
                try {
                    it.getPositions(address)
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                    emptyList()
                }
            }.map { it.toVO() }
    }

    suspend fun BorrowPosition.toVO(): BorrowPositionVO {
        return with(this) {
            BorrowPositionVO(
                network = market.network.toNetworkInformation(),
                dollarValue = priceResource.calculatePrice(
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