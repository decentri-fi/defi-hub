package io.defitrack.price

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.hop.HopPriceService
import io.defitrack.protocol.balancer.graph.BalancerPolygonPoolGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class PriceCalculator(
    private val erc20Service: ERC20Resource,
    private val balancerTokenService: BalancerPolygonPoolGraphProvider,
    private val hopPriceService: HopPriceService,
    private val priceProvider: PriceProvider
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun calculatePrice(
        priceRequest: PriceRequest
    ): Double {
        if (priceRequest.amount == BigDecimal.ZERO) {
            return 0.0
        }

        val tokenAddress = if (priceRequest.address == "0x0") {
            erc20Service.getWrappedToken(priceRequest.network).address
        } else {
            priceRequest.address
        }

        if (tokenAddress == null) {
            logger.error("Token address is null for ${priceRequest.address} and network ${priceRequest.network}")
            return 0.0
        }

        return retry(limitAttempts(5)) {
            val token = erc20Service.getTokenInformation(priceRequest.network, tokenAddress)

            val tokenType = (priceRequest.type ?: token.type)
            val price = when {
                tokenType.standardLpToken -> {
                    calculateLpHoldings(priceRequest, token)
                }

                tokenType == TokenType.BALANCER -> {
                    calculateBalancerPrice(priceRequest)
                }

                tokenType == TokenType.CURVE -> {
                    calculateLpHoldings(priceRequest, token)
                }

                tokenType == TokenType.HOP -> {
                    hopPriceService.calculateHopPrice(priceRequest)
                }

                else -> {
                    calculateTokenWorth(
                        priceRequest.network,
                        token,
                        priceRequest.amount
                    )
                }
            }


            price.times(
                BigDecimal.TEN.pow(18)
            ).dividePrecisely(BigDecimal.TEN.pow(18)).toDouble()
        }
    }

    private suspend fun calculateBalancerPrice(priceRequest: PriceRequest) =
        balancerTokenService.getPool(priceRequest.address)?.let { pool ->
            pool.totalLiquidity.divide(pool.totalShares, 18, RoundingMode.HALF_UP)
                .times(priceRequest.amount)
        } ?: BigDecimal.ZERO


    private suspend fun calculateLpHoldings(
        priceRequest: PriceRequest,
        tokenInformation: TokenInformationVO
    ): BigDecimal {
        return try {
            return calculateLpHoldings(
                priceRequest.network,
                tokenInformation.address,
                priceRequest.amount,
                tokenInformation.totalSupply,
                tokenInformation.underlyingTokens
            )
        } catch (ex: Exception) {
            logger.error("Unable to calculate price for ${priceRequest.address}")
            ex.printStackTrace()
            BigDecimal.ZERO
        }
    }

    suspend fun calculateTokenWorth(
        network: Network,
        token: TokenInformationVO,
        amount: BigDecimal,
    ): BigDecimal {
        val tokenPrice = priceProvider.getPrice(token)
        return amount.times(tokenPrice)
    }


    suspend fun calculateLpHoldings(
        network: Network,
        lpAddress: String,
        userLPAmount: BigDecimal,
        totalLPAmount: BigInteger,
        underlyingTokens: List<TokenInformationVO>
    ): BigDecimal {

        val userShare = userLPAmount.dividePrecisely(
            totalLPAmount.dividePrecisely(BigDecimal.TEN.pow(18))
        )

        return underlyingTokens.map { underlyingToken ->
            val price = priceProvider.getPrice(underlyingToken)
            val underlyingTokenBalance = erc20Service.getBalance(network, underlyingToken.address, lpAddress)
            val userTokenAmount = underlyingTokenBalance.toBigDecimal().times(userShare)

            userTokenAmount.dividePrecisely(
                BigDecimal.TEN.pow(underlyingToken.decimals)
            ).times(price)
        }.reduce { acc, bigDecimal ->
            acc.plus(bigDecimal)
        }
    }
}