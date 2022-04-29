package io.defitrack.balance

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.token.ERC20Resource
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping
class BalanceRestController(
    private val balanceServices: List<BalanceService>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) {


    @Deprecated("use the network-specific call")
    @GetMapping("/{address}/native-balance")
    fun getBalance(@PathVariable("address") address: String): List<BalanceElement> =
        balanceServices.mapNotNull {
            val balance = try {
                it.getNativeBalance(address)
            } catch (ex: Exception) {
                BigDecimal.ZERO
            }

            if (balance > BigDecimal.ZERO) {
                BalanceElement(
                    amount = balance.toDouble(),
                    network = it.getNetwork().toVO(),
                    token = erC20Resource.getTokenInformation(
                        it.getNetwork(),
                        "0x0"
                    ).toFungibleToken(),
                    dollarValue = priceResource.calculatePrice(
                        it.nativeTokenName(),
                        balance.toDouble()
                    ),
                )
            } else {
                null
            }
        }

    @GetMapping(value = ["/{address}/native-balance"], params = ["network"])
    fun getBalanceByNetwork(
        @PathVariable("address") address: String,
        @RequestParam("network") network: Network
    ): BalanceElement {
        val balanceService = balanceServices.first {
            it.getNetwork() == network
        }

        val balance = balanceService.getNativeBalance(address)

        return BalanceElement(
            amount = balance.toDouble(),
            network = network.toVO(),
            token = erC20Resource.getTokenInformation(
                network,
                "0x0"
            ).toFungibleToken(),
            dollarValue = priceResource.calculatePrice(
                balanceService.nativeTokenName(),
                balance.toDouble()
            ),
        )
    }

    @GetMapping("/{address}/token-balances")
    fun getTokenBalance(@PathVariable("address") address: String): List<BalanceElement> = balanceServices.flatMap {
        try {
            it.getTokenBalances(address)
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }.map {
        val normalizedAmount =
            it.amount.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(it.token.decimals)).toDouble()
        BalanceElement(
            amount = normalizedAmount,
            network = it.network.toVO(),
            token = it.token,
            dollarValue = priceResource.calculatePrice(it.token.symbol, normalizedAmount)
        )
    }

    @GetMapping(value = ["/{address}/token-balances"], params = ["network"])
    fun getTokenBalanceByNetwork(
        @PathVariable("address") address: String,
        @RequestParam("network") network: Network
    ): List<BalanceElement> {
        val balanceService = balanceServices.first {
            it.getNetwork() == network
        }

        return balanceService.getTokenBalances(address).map {
            val normalizedAmount =
                it.amount.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(it.token.decimals)).toDouble()
            BalanceElement(
                amount = normalizedAmount,
                network = it.network.toVO(),
                token = it.token,
                dollarValue = priceResource.calculatePrice(it.token.symbol, normalizedAmount)
            )
        }
    }
}