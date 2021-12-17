package io.defitrack.lending

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.lending.domain.LendingElement
import io.defitrack.lending.vo.LendingElementVO
import io.defitrack.network.toVO
import io.defitrack.protocol.toVO
import io.defitrack.price.PriceResource
import io.defitrack.common.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lending")
class DefaultLendingRestController(
    private val lendingServices: List<LendingService>,
    private val priceResource: PriceResource
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(@PathVariable("userId") address: String): List<LendingElementVO> {
        return lendingServices.flatMap {
            try {
                it.getLendings(address)
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                emptyList()
            }
        }.map { it.toVO() }
    }

    @GetMapping(value = ["/{userId}/positions"], params = ["lendingElementId", "network"])
    fun getStakingById(
        @PathVariable("userId") address: String,
        @RequestParam("lendingElementId") lendingElementId: String,
        @RequestParam("network") network: Network
    ): LendingElementVO? {
        return lendingServices.filter {
            it.getNetwork() == network
        }.mapNotNull {
            try {
                runBlocking(Dispatchers.IO) {
                    retry(limitAttempts(3)) {
                        it.getLending(address, lendingElementId)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Something went wrong trying to fetch the user lendings: ${ex.message}")
                null
            }
        }.firstOrNull()?.toVO()
    }

    fun LendingElement.toVO(): LendingElementVO {
        return with(this) {
            LendingElementVO(
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
                id = id
            )
        }
    }
}