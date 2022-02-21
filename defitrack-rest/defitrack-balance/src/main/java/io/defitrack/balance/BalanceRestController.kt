package io.defitrack.balance

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.logo.LogoService
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
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
                    token = FungibleToken(
                        address = "0x0",
                        name = it.nativeTokenName(),
                        decimals = 18,
                        symbol = it.nativeTokenName(),
                        type = TokenType.SINGLE
                    ),
                    dollarValue = priceResource.calculatePrice(
                        it.nativeTokenName(),
                        balance.toDouble()
                    ),
                    logo = logoService.generateLogoUrl(it.getNetwork(), "0x0"),
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
            normalizedAmount,
            it.network.toVO(),
            it.token,
            priceResource.calculatePrice(it.token.symbol, normalizedAmount),
            logoService.generateLogoUrl(it.network, it.token.address),
        )
    }
}