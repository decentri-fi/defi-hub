package io.defitrack.market.lending

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.borrowing.BorrowService
import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.market.borrowing.vo.BorrowPositionVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/{protocol}/borrowing")
class DefaultBorrowingRestController(
    private val borrowingServices: List<BorrowService>,
    private val priceResource: PriceResource,
    private val protocolVOMapper: ProtocolVOMapper
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(
        @PathVariable("protocol") protocol: String,
        @PathVariable("userId") address: String
    ): List<BorrowPositionVO> =
        runBlocking {
            borrowingServices
                .filter {
                    it.getProtocol().slug == protocol
                }
                .flatMap {
                    try {
                        it.getBorrows(address)
                    } catch (ex: Exception) {
                        logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                        emptyList()
                    }
                }.map { it.toVO() }
        }

    suspend fun BorrowPosition.toVO(): BorrowPositionVO {
        return with(this) {
            BorrowPositionVO(
                network = network.toVO(),
                dollarValue = priceResource.calculatePrice(
                    PriceRequest(
                        token.address,
                        this.network,
                        this.amount.asEth(token.decimals),
                        token.type
                    )
                ),
                protocol = protocolVOMapper.map(protocol),
                rate = rate,
                name = name,
                amount = amount.asEth(token.decimals).toDouble(),
                id = id,
                token = token
            )
        }
    }
}