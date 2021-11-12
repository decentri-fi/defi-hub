package io.codechef.defitrack.lending

import io.codechef.defitrack.borrowing.BorrowService
import io.codechef.defitrack.borrowing.domain.BorrowElement
import io.codechef.defitrack.borrowing.vo.BorrowElementVO
import io.codechef.defitrack.network.toVO
import io.codechef.defitrack.protocol.toVO
import io.defitrack.abi.PriceResource
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
    fun getPoolingMarkets(@PathVariable("userId") address: String): List<BorrowElementVO> {
        return borrowingServices.flatMap {
            try {
                it.getBorrows(address)
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                emptyList()
            }
        }.map { it.toVO() }
    }

    fun BorrowElement.toVO(): BorrowElementVO {
        return with(this) {
            BorrowElementVO(
                user = user,
                network = network.toVO(),
                protocol = protocol.toVO(),
                dollarValue = priceResource.calculatePrice(
                    symbol,
                    amount.toDouble()
                ),
                rate = rate,
                name = name,
                amount = amount,
                symbol = symbol,
                tokenUrl = tokenUrl
            )
        }
    }
}