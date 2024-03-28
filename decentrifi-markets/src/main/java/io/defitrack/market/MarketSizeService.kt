package io.defitrack.market

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.common.network.Network
import io.defitrack.domain.BalanceResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class MarketSizeService(
    private val balanceResource: BalanceResource,
) {

    suspend fun getMarketSizeInUSD(
        tokens: List<FungibleTokenInformation>,
        location: String,
        network: Network
    ): BigDecimal {
        return tokens.sumOf {
            getMarketSize(it, location, network).usdAmount
        }
    }

    suspend fun getMarketSize(
        token: FungibleTokenInformation,
        location: String,
        network: Network
    ): MarketSize {
        val balance = balanceResource.getTokenBalance(network, location, token.address)

        return MarketSize(
            tokenAmount = balance.amount,
            usdAmount = balance.dollarValue.toBigDecimal()
        )
    }

    data class MarketSize(
        val tokenAmount: BigInteger,
        val usdAmount: BigDecimal
    )
}