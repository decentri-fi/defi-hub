package io.defitrack.price.application

import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.retry
import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.domain.BalanceResource
import io.defitrack.port.output.ERC20Client
import io.defitrack.price.port.`in`.PriceCalculator
import io.defitrack.token.TokenType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
class DefaultPriceCalculator(
    private val ERC20Client: ERC20Client,
    private val balanceResource: BalanceResource,
    private val aggregatedPriceProvider: AggregatedPriceProvider
) : PriceCalculator {

    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun calculatePrice(
        getPriceCommand: GetPriceCommand
    ): Double {
        if (getPriceCommand.amount == BigDecimal.ZERO) {
            return 0.0
        }

        val tokenAddress = if (getPriceCommand.address == "0x0") {
            ERC20Client.getWrappedToken(getPriceCommand.network).address
        } else {
            getPriceCommand.address
        }

        if (tokenAddress == null) {
            logger.error("Token address is null for ${getPriceCommand.address} and network ${getPriceCommand.network}")
            return 0.0
        }

        return retry(limitAttempts(5)) {
            val token = ERC20Client.getTokenInformation(getPriceCommand.network, tokenAddress)

            val tokenPrice = aggregatedPriceProvider.getPrice(token.address, token.network.toNetwork())

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
            val price = aggregatedPriceProvider.getPrice(underlyingToken.address, underlyingToken.network.toNetwork())
            val underlyingTokenBalance = balanceResource.getTokenBalance(network, lpAddress, underlyingToken.address)
            val userTokenAmount = underlyingTokenBalance.amount.toBigDecimal().times(userShare)

            userTokenAmount.dividePrecisely(
                BigDecimal.TEN.pow(underlyingToken.decimals)
            ).times(price)
        }.reduce { acc, bigDecimal ->
            acc.plus(bigDecimal)
        }
    }
}