package io.defitrack.balance.rest

import io.defitrack.balance.service.BalanceService
import io.defitrack.balance.service.dto.BalanceElement
import io.defitrack.balance.service.dto.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping
class BalanceRestController(
    private val balanceServices: List<BalanceService>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Deprecated("use the network-specific call")
    @GetMapping("/{address}/native-balance")
    suspend fun getBalance(@PathVariable("address") address: String): List<BalanceElement> = coroutineScope {
        balanceServices.map {
            async {
                try {
                    val balance = it.getNativeBalance(address)

                    if (balance > BigDecimal.ZERO) {
                        val price = priceResource.calculatePrice(
                            PriceRequest(
                                "0x0", it.getNetwork(), 1.0.toBigDecimal()
                            )
                        )
                        BalanceElement(
                            amount = balance.toDouble(),
                            network = it.getNetwork().toVO(),
                            token = erC20Resource.getTokenInformation(
                                it.getNetwork(), "0x0"
                            ).toFungibleToken(),
                            dollarValue = price.times(balance.toDouble()),
                            price = price
                        )
                    } else {
                        null
                    }
                } catch (ex: Exception) {
                    log.error("Unable to fetch balance for ${it.getNetwork()}", ex)
                    null
                }

            }
        }.awaitAll().filterNotNull()
    }

    @GetMapping(value = ["/{address}/native-balance"], params = ["network"])
    suspend fun getBalanceByNetwork(
        @PathVariable("address") address: String, @RequestParam("network") networkName: String
    ): ResponseEntity<BalanceElement> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.notFound().build()

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

        return ResponseEntity.ok(
            BalanceElement(
                amount = balance.toDouble(), network = network.toVO(), token = erC20Resource.getTokenInformation(
                    network, "0x0"
                ).toFungibleToken(), dollarValue = price.times(balance.toDouble()), price = price
            )
        )
    }

    @GetMapping("/{address}/token-balances")
    suspend fun getTokenBalance(@PathVariable("address") address: String): List<BalanceElement> = coroutineScope {
        balanceServices.map {
            async {
                try {
                    it.getTokenBalances(address)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    emptyList()
                }
            }
        }.awaitAll().flatten()
            .distinctBy {
                it.token
            }
            .map { it.toBalanceElement() }
    }

    @GetMapping(value = ["/{address}/token-balances"], params = ["network"])
    suspend fun getTokenBalanceByNetwork(
        @PathVariable("address") address: String, @RequestParam("network") network: Network
    ): List<BalanceElement> {
        val balanceService = balanceServices.firstOrNull {
            it.getNetwork() == network
        }

        return balanceService?.getTokenBalances(address)?.map { it.toBalanceElement() } ?: emptyList()
    }

    suspend fun TokenBalance.toBalanceElement(): BalanceElement {
        val normalizedAmount = amount.asEth(token.decimals).toDouble()
        val price = priceResource.calculatePrice(
            PriceRequest(
                token.address, network, 1.0.toBigDecimal()
            )
        )
        return BalanceElement(
            amount = normalizedAmount,
            network = network.toVO(),
            token = token,
            dollarValue = price.times(normalizedAmount),
            price = price
        )
    }
}