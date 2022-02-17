package io.defitrack.balance

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.logo.LogoService
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
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
    private val logoService: LogoService
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
                    name = it.nativeTokenName(),
                    address = "0x0",
                    dollarValue = priceResource.calculatePrice(
                        it.nativeTokenName(),
                        balance.toDouble()
                    ),
                    logo = logoService.generateLogoUrl(it.getNetwork(), "0x0"),
                    symbol = it.nativeTokenName()
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
            it.amount.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(it.decimals)).toDouble()
        BalanceElement(
            normalizedAmount,
            it.network.toVO(),
            it.address,
            it.symbol,
            it.name,
            priceResource.calculatePrice(it.symbol, normalizedAmount),
            logoService.generateLogoUrl(it.network, it.address),
        )
    }
}