package io.defitrack.price

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class PriceCalculator(
    private val erc20Service: ERC20Resource,
    private val beefyPriceService: BeefyPricesService,
    private val coinGeckoPriceService: CoinGeckoPriceService,
    private val balancerTokenService: BalancerPolygonService,
    private val externalPriceServices: List<ExternalPriceService>
) {

    val synonyms = mapOf(
        "WETH" to "ETH",
        "WMATIC" to "MATIC",
        "miMATIC" to "MAI",
        "WBTC" to "BTC"
    )

    fun calculatePrice(
        priceRequest: PriceRequest?
    ): Double {
        return runBlocking {
            retry(limitAttempts(5)) {
                if (priceRequest == null) {
                    return@retry 0.0
                }

                val asERC = erc20Service.getTokenInformation(priceRequest.network, priceRequest.address)
                val token = erc20Service.getTokenInformation(priceRequest.network, priceRequest.address)

                val tokenType = (priceRequest.type ?: token.type)
                val price = when {
                    tokenType.standardLpToken -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    tokenType == TokenType.BALANCER -> {
                        calculateBalancerPrice(priceRequest)
                    }
                    else -> {
                        calculateTokenWorth(
                            asERC.symbol,
                            priceRequest.amount
                        )
                    }
                }


                price.times(
                    BigDecimal.TEN.pow(18)
                ).divide(BigDecimal.TEN.pow(18), 6, RoundingMode.HALF_UP).toDouble()
            }
        }
    }

    private fun calculateBalancerPrice(priceRequest: PriceRequest) =
        balancerTokenService.getPool(priceRequest.address)?.let { pool ->
            pool.totalLiquidity.divide(pool.totalShares, 18, RoundingMode.HALF_UP)
                .times(priceRequest.amount)
        } ?: BigDecimal.ZERO

    private fun calculateLpPrice(
        priceRequest: PriceRequest,
        tokenInformation: TokenInformation
    ) = calculateLPWorth(
        priceRequest.network,
        tokenInformation.address,
        priceRequest.amount,
        tokenInformation.totalSupply,
        tokenInformation.tokenInformation0!!,
        tokenInformation.tokenInformation1!!
    )

    fun calculateTokenWorth(
        symbol: String,
        amount: BigDecimal,
    ): BigDecimal {
        val tokenPrice = getPrice(symbol)
        return amount.times(tokenPrice)
    }

    fun getPrice(symbol: String): BigDecimal {
        return externalPriceServices.find {
            it.appliesTo(symbol)
        }?.getPrice() ?: beefyPriceService.getPrices()
            .getOrDefault(synonyms.getOrDefault(symbol.uppercase(), symbol.uppercase()), null) ?: runBlocking(
            Dispatchers.IO
        ) { coinGeckoPriceService.getPrice(symbol) } ?: BigDecimal.ZERO
    }

    fun calculateLPWorth(
        network: Network,
        lpAddress: String,
        userLPAmount: BigDecimal,
        totalLPAmount: BigInteger,
        tokenInformation0: TokenInformation,
        tokenInformation1: TokenInformation,
    ): BigDecimal {

        val token0Price = getPrice(tokenInformation0.symbol)
        val token1Price = getPrice(tokenInformation1.symbol)

        val userShare =
            userLPAmount.divide(totalLPAmount.toBigDecimal().divide(BigDecimal.TEN.pow(18)), 18, RoundingMode.HALF_UP)

        val lpToken0Amount = erc20Service.getBalance(network, tokenInformation0.address, lpAddress)
        val lpToken1Amount = erc20Service.getBalance(network, tokenInformation1.address, lpAddress)

        val userToken0Amount = lpToken0Amount.toBigDecimal().times(userShare)
        val userToken1Amount = lpToken1Amount.toBigDecimal().times(userShare)

        val totalDollarValueToken0 = userToken0Amount.div(
            BigDecimal.TEN.pow(tokenInformation0.decimals)
        ).times(token0Price)
        val totalDollarValueToken1 = userToken1Amount.div(
            BigDecimal.TEN.pow(tokenInformation1.decimals)
        ).times(token1Price)

        val multiplier =
            if (totalDollarValueToken0.toBigInteger() == BigInteger.ZERO || totalDollarValueToken1.toBigInteger() == BigInteger.ZERO) {
                2
            } else {
                1
            }
        return (totalDollarValueToken0 + totalDollarValueToken1).times(multiplier.toBigDecimal())
    }
}