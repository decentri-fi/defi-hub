package io.defitrack.balance

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.token.ERC20Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping
class BalanceRestController(
    private val balanceServices: List<BalanceService>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
) {

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
                    ),
                    dollarValue = priceResource.calculatePrice(
                        it.nativeTokenName(),
                        balance.toDouble()
                    ),
                )
            } else {
                null
            }
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
            token = erC20Resource.getTokenInformation(it.network, address),
            dollarValue = priceResource.calculatePrice(it.token.symbol, normalizedAmount)
        )
    }
}