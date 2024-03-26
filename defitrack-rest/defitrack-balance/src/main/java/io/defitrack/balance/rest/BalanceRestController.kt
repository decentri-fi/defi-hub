package io.defitrack.balance.rest

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.balance.service.BalanceService
import io.defitrack.balance.service.dto.BalanceElement
import io.defitrack.balance.service.dto.TokenBalance
import io.defitrack.balance.service.token.ERC20BalanceService
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.price.port.`in`.PricePort
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.math.BigInteger

@RestController
@RequestMapping
class BalanceRestController(
    private val balanceServices: List<BalanceService>,
    private val priceResource: PricePort,
    private val erC20Resource: ERC20Resource,
    private val erC20BalanceService: ERC20BalanceService
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{user}/native-balance")
    suspend fun getBalance(@PathVariable("user") address: String): List<BalanceElement> {
        return balanceServices.parMapNotNull(concurrency = 8) {
            try {
                val balance = it.getNativeBalance(address)

                if (balance > BigInteger.ZERO) {
                    val price = priceResource.calculatePrice(
                        GetPriceCommand(
                            "0x0", it.getNetwork(), 1.0.toBigDecimal()
                        )
                    )
                    BalanceElement(
                        amount = balance,
                        network = it.getNetwork().toNetworkInformation(),
                        token = erC20Resource.getTokenInformation(
                            it.getNetwork(), "0x0"
                        ),
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

    @GetMapping(value = ["/{user}/native-balance"], params = ["network"])
    suspend fun getNativeBalanceByNetwork(
        @PathVariable("user") address: String,
        @RequestParam("network") networkName: String
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
                amount = balance,
                network = network.toNetworkInformation(),
                token = erC20Resource.getTokenInformation(
                    network, "0x0"
                ),
                price = price
            )
        )
    }


    @GetMapping("/{user}/{token}")
    suspend fun getTokenBalance(
        @PathVariable("user") user: String,
        @RequestParam("network") networkName: String,
        @PathVariable("token") token: String
    ): ResponseEntity<BalanceElement> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(user)) {
            return ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(token)) {
            return ResponseEntity.badRequest().build()
        }

        return if (token == "0x0" || token == "0x0000000000000000000000000000000000000000") {
            return getNativeBalanceByNetwork(user, networkName)
        } else {
            val balance = erC20BalanceService.getBalance(network, token, user)
            val tokenInfo = erC20Resource.getTokenInformation(network, token)
            val price = priceResource.calculatePrice(
                GetPriceCommand(
                    tokenInfo.address, network, 1.0.toBigDecimal()
                )
            )
            ResponseEntity.ok(
                BalanceElement(
                    amount = balance,
                    network = network.toNetworkInformation(),
                    token = tokenInfo,
                    price = price
                )
            )
        }
    }

    @GetMapping("/{address}/token-balances")
    suspend fun getTokenBalances(@PathVariable("address") address: String): List<BalanceElement> = coroutineScope {
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
        val price = priceResource.calculatePrice(
            GetPriceCommand(
                token.address, network, 1.0.toBigDecimal()
            )
        )
        return BalanceElement(
            amount = amount,
            network = network.toNetworkInformation(),
            token = token,
            price = price
        )
    }
}