package io.defitrack.market.lending

import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.market.borrowing.vo.BorrowPositionVO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.borrowing.BorrowService
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/borrowing")
class DefaultBorrowingRestController(
    private val borrowingServices: List<BorrowService>,
    private val priceResource: PriceResource
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(@PathVariable("userId") address: String): List<BorrowPositionVO> =
        runBlocking(Dispatchers.IO) {
            borrowingServices.flatMap {
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
                protocol = protocol.toVO(),
                dollarValue = priceResource.calculatePrice(
                    token.symbol,
                    amount.toDouble()
                ),
                rate = rate,
                name = name,
                amount = amount.asEth(token.decimals).toDouble(),
                id = id,
                token = token
            )
        }
    }
}