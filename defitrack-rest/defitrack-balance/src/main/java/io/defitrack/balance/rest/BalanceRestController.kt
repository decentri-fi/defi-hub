package io.defitrack.balance.rest

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.balance.service.BalanceService
import io.defitrack.balance.service.dto.BalanceElement
import io.defitrack.balance.service.dto.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.domain.GetPriceCommand
import io.defitrack.domain.toNetworkInformation
import io.defitrack.port.input.ERC20Resource
import io.defitrack.port.input.PriceResource
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
    suspend fun getBalance(@PathVariable("address") address: String): List<BalanceElement> {
        return balanceServices.parMapNotNull {
            try {
                val balance = it.getNativeBalance(address)

                if (balance > BigDecimal.ZERO) {
                    val price = priceResource.calculatePrice(
                        GetPriceCommand(
                            "0x0", it.getNetwork(), 1.0.toBigDecimal()
                        )
                    )
                    BalanceElement(
                        amount = balance.toDouble(),
                        network = it.getNetwork().toNetworkInformation(),
                        token = erC20Resource.getTokenInformation(
                            it.getNetwork(), "0x0"
                        ),
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
            GetPriceCommand(
                "0x0",
                balanceService.getNetwork(),
                1.0.toBigDecimal(),
            )
        )

        return ResponseEntity.ok(
            BalanceElement(
                amount = balance.toDouble(), network = network.toNetworkInformation(), token = erC20Resource.getTokenInformation(
                    network, "0x0"
                ), dollarValue = price.times(balance.toDouble()), price = price
            )
        )
    }

    @GetMapping("/{address}/token-balances")
    suspend fun getTokenBalance(@PathVariable("address") address: String): List<BalanceElement> = coroutineScope {
        balanceServices.parMap {
            try {
                it.getTokenBalances(address)
            } catch (ex: Exception) {
                ex.printStackTrace()
                emptyList()
            }
        }.flatten()
            .distinctBy {
                it.token.address
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
            GetPriceCommand(
                token.address, network, 1.0.toBigDecimal()
            )
        )
        return BalanceElement(
            amount = normalizedAmount,
            network = network.toNetworkInformation(),
            token = token,
            dollarValue = price.times(normalizedAmount),
            price = price
        )
    }
}