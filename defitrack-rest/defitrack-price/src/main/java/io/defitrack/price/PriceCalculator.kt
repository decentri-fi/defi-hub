package io.defitrack.price

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.BalancerPolygonService
import io.defitrack.protocol.staking.LpToken
import io.defitrack.protocol.staking.Token
import io.defitrack.protocol.staking.TokenType
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class PriceCalculator(
    private val erc20Service: ERC20Resource,
    private val priceRepository: BeefyPricesService,
    private val balancerTokenService: BalancerPolygonService,
    private val externalPriceServices: List<ExternalPriceService>
) {

    val synonyms = mapOf(
        "WETH" to "ETH",
        "WMATIC" to "MATIC",
        "miMATIC" to "MAI"
    )

    fun calculatePrice(
        priceRequest: PriceRequest?
    ): Double {
        return runBlocking {
            retry(limitAttempts(5)) {
                if (priceRequest == null) {
                    return@retry 0.0
                }

                val asERC = erc20Service.getERC20(priceRequest.network, priceRequest.address)
                val token = erc20Service.getTokenInformation(priceRequest.network, priceRequest.address)

                val price = when (priceRequest.type ?: token.type) {
                    TokenType.SUSHISWAP -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.UNISWAP -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.DMM -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.WAULT -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.DFYN -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.KYBER -> {
                        calculateLpPrice(priceRequest, token)
                    }
                    TokenType.BALANCER -> {
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
        token: Token
    ) = calculateLPWorth(
        priceRequest.network,
        token.address,
        priceRequest.amount,
        (token as LpToken).totalSupply,
        token.token0,
        token.token1
    )

    fun calculateTokenWorth(
        symbol: String,
        amount: BigDecimal,
    ): BigDecimal {
        val tokenPrice = getPrice(symbol)
        return amount.times(tokenPrice)
    }

    fun getPrice(name: String): BigDecimal {
        return externalPriceServices.find {
            it.appliesTo(name)
        }?.getPrice() ?: priceRepository.getPrices()
            .getOrDefault(synonyms.getOrDefault(name.uppercase(), name.uppercase()), BigDecimal.ZERO)
    }

    fun calculateLPWorth(
        network: Network,
        lpAddress: String,
        userLPAmount: BigDecimal,
        totalLPAmount: BigInteger,
        token0: Token,
        token1: Token,
    ): BigDecimal {

        val token0Price = getPrice(token0.symbol)
        val token1Price = getPrice(token1.symbol)

        val userShare =
            userLPAmount.divide(totalLPAmount.toBigDecimal().divide(BigDecimal.TEN.pow(18)), 18, RoundingMode.HALF_UP)

        val lpToken0Amount = erc20Service.getBalance(network, token0.address, lpAddress)
        val lpToken1Amount = erc20Service.getBalance(network, token1.address, lpAddress)

        val userToken0Amount = lpToken0Amount.toBigDecimal().times(userShare)
        val userToken1Amount = lpToken1Amount.toBigDecimal().times(userShare)

        val totalDollarValueToken0 = userToken0Amount.div(
            BigDecimal.TEN.pow(token0.decimals)
        ).times(token0Price)
        val totalDollarValueToken1 = userToken1Amount.div(
            BigDecimal.TEN.pow(token1.decimals)
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