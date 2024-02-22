package io.defitrack.price.application

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.token.TokenType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Component
class DefaultPriceCalculator(
    private val erc20Service: ERC20Resource,
    private val aggregatedPriceProvider: AggregatedPriceProvider
) : PriceCalculator{

    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun calculatePrice(
        getPriceCommand: GetPriceCommand
    ): Double {
        if (getPriceCommand.amount == BigDecimal.ZERO) {
            return 0.0
        }

        val tokenAddress = if (getPriceCommand.address == "0x0") {
            erc20Service.getWrappedToken(getPriceCommand.network).address
        } else {
            getPriceCommand.address
        }

        if (tokenAddress == null) {
            logger.error("Token address is null for ${getPriceCommand.address} and network ${getPriceCommand.network}")
            return 0.0
        }

        return retry(limitAttempts(5)) {
            val token = erc20Service.getTokenInformation(getPriceCommand.network, tokenAddress)

            val tokenPrice = aggregatedPriceProvider.getPrice(token)

            val price = if (tokenPrice != null) {
                getPriceCommand.amount.times(tokenPrice)
            } else {
                val tokenType = (getPriceCommand.type ?: token.type)
                when (tokenType) {
                    TokenType.STANDARD_LP -> {
                        calculateLpHolding(getPriceCommand, token)
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
        getPriceCommand: GetPriceCommand,
        tokenInformation: FungibleTokenInformation
    ): BigDecimal {
        return try {
            return calculateSingleLpHolding(
                getPriceCommand.network,
                tokenInformation.address,
                getPriceCommand.amount,
                tokenInformation.totalSupply,
                tokenInformation.underlyingTokens
            )
        } catch (ex: Exception) {
            logger.error("Unable to calculate price for ${getPriceCommand.address}: {}", ex.message)
            BigDecimal.ZERO
        }
    }

   private suspend fun calculateSingleLpHolding(
        network: Network,
        lpAddress: String,
        userLPAmount: BigDecimal,
        totalLPAmount: BigInteger,
        underlyingTokens: List<FungibleTokenInformation>
    ): BigDecimal {
        val userShare = userLPAmount.dividePrecisely(
            totalLPAmount.dividePrecisely(BigDecimal.TEN.pow(18))
        )

        return underlyingTokens.map { underlyingToken ->
            val price = aggregatedPriceProvider.getPrice(underlyingToken)
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