package io.defitrack.lending

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.lending.domain.LendingPosition
import io.defitrack.lending.vo.LendingElementVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lending")
class DefaultLendingRestController(
    private val lendingUserServices: List<LendingUserService>,
    private val priceResource: PriceResource
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/{userId}/positions")
    fun getPoolingMarkets(@PathVariable("userId") address: String): List<LendingElementVO> =
        runBlocking(Dispatchers.IO) {
            lendingUserServices.flatMap {
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
        return lendingUserServices.filter {
            it.getNetwork() == network
        }.firstNotNullOfOrNull {
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
        }?.toVO()
    }

    fun LendingPosition.toVO(): LendingElementVO {
        return with(this) {

            val lendingInDollars = priceResource.calculatePrice(
                PriceRequest(
                    address = market.token.address,
                    network = market.network,
                    amount = amount.asEth(market.token.decimals),
                    type = null
                )
            )

            LendingElementVO(
                network = market.network.toVO(),
                protocol = market.protocol.toVO(),
                dollarValue = lendingInDollars,
                rate = market.rate?.toDouble(),
                name = market.name,
                amount = amount.asEth(market.token.decimals).toDouble(),
                id = market.id,
                token = market.token
            )
        }
    }
}