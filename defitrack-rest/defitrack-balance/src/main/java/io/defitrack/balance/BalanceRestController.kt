package io.defitrack.balance

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping
class BalanceRestController(
    private val balanceServices: List<BalanceService>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Deprecated("use the network-specific call")
    @GetMapping("/{address}/native-balance")
    fun getBalance(@PathVariable("address") address: String): List<BalanceElement> = runBlocking {
        balanceServices.mapNotNull {
            val balance = try {
                it.getNativeBalance(address)
            } catch (ex: Exception) {
                BigDecimal.ZERO
            }

            if (balance > BigDecimal.ZERO) {
                val price = priceResource.calculatePrice(
                    PriceRequest(
                        "0x0",
                        it.getNetwork(),
                        1.0.toBigDecimal()
                    )
                )
                BalanceElement(
                    amount = balance.toDouble(),
                    network = it.getNetwork().toVO(),
                    token = erC20Resource.getTokenInformation(
                        it.getNetwork(),
                        "0x0"
                    ).toFungibleToken(),
                    dollarValue = price.times(balance.toDouble()),
                    price = price
                )
            } else {
                null
            }
        }
    }

    @GetMapping(value = ["/{address}/native-balance"], params = ["network"])
    fun getBalanceByNetwork(
        @PathVariable("address") address: String,
        @RequestParam("network") network: Network
    ): BalanceElement = runBlocking {
        val balanceService = balanceServices.first {
            it.getNetwork() == network
        }

        val balance = balanceService.getNativeBalance(address)
        val price = priceResource.calculatePrice(
            PriceRequest(
                "0x0",
                balanceService.getNetwork(),
                1.0.toBigDecimal(),
            )
        )

        BalanceElement(
            amount = balance.toDouble(),
            network = network.toVO(),
            token = erC20Resource.getTokenInformation(
                network,
                "0x0"
            ).toFungibleToken(),
            dollarValue = price.times(balance.toDouble()),
            price = price
        )
    }

    @GetMapping("/{address}/token-balances")
    fun getTokenBalance(@PathVariable("address") address: String): List<BalanceElement> = runBlocking {
        balanceServices.flatMap {
            try {
                it.getTokenBalances(address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }.map {
            val normalizedAmount = it.amount.asEth(it.token.decimals).toDouble()
            val price = priceResource.calculatePrice(
                PriceRequest(
                    it.token.address,
                    it.network,
                    1.0.toBigDecimal()
                )
            )

            BalanceElement(
                amount = normalizedAmount,
                network = it.network.toVO(),
                token = it.token,
                dollarValue = price.times(normalizedAmount),
                price = price
            )
        }
    }

    @GetMapping(value = ["/{address}/token-balances"], params = ["network"])
    fun getTokenBalanceByNetwork(
        @PathVariable("address") address: String,
        @RequestParam("network") network: Network
    ): List<BalanceElement> = runBlocking(Dispatchers.IO) {
        val balanceService = balanceServices.firstOrNull {
            it.getNetwork() == network
        }

        balanceService?.getTokenBalances(address)?.map {
            val normalizedAmount = it.amount.asEth(it.token.decimals).toDouble()
            val price = priceResource.calculatePrice(
                PriceRequest(
                    it.token.address,
                    it.network,
                    1.0.toBigDecimal()
                )
            )
            BalanceElement(
                amount = normalizedAmount,
                network = it.network.toVO(),
                token = it.token,
                dollarValue = price.times(normalizedAmount),
                price = price
            )
        } ?: kotlin.run {
            logger.error("no balance service found for network $network")
            emptyList()
        }
    }
}