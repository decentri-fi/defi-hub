package io.defitrack.price

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.token.FungibleToken
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class PriceCalculator(
    private val erc20Service: ERC20Resource,
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

            val tokenPrice = priceProvider.getPrice(token)

            val price = if (tokenPrice != null) {
                priceRequest.amount.times(tokenPrice)
            } else {
                val tokenType = (priceRequest.type ?: token.type)
                when (tokenType) {
                    TokenType.STANDARD_LP -> {
                        calculateLpHolding(priceRequest, token)
                    }

                    else -> {
                        BigDecimal.ZERO
                    }
                }
            }
            price.toDouble()
        }
    }

    private suspend fun calculateLpHolding(
        priceRequest: PriceRequest,
        tokenInformation: FungibleToken
    ): BigDecimal {
        return try {
            return calculateSingleLpHolding(
                priceRequest.network,
                tokenInformation.address,
                priceRequest.amount,
                tokenInformation.totalSupply,
                tokenInformation.underlyingTokens
            )
        } catch (ex: Exception) {
            logger.error("Unable to calculate price for ${priceRequest.address}: {}", ex.message)
            BigDecimal.ZERO
        }
    }

    suspend fun calculateSingleLpHolding(
        network: Network,
        lpAddress: String,
        userLPAmount: BigDecimal,
        totalLPAmount: BigInteger,
        underlyingTokens: List<FungibleToken>
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