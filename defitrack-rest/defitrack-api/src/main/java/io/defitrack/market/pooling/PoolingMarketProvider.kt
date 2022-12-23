package io.defitrack.market.pooling

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.invest.MarketProvider
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.math.BigInteger

abstract class PoolingMarketProvider(val erc20Resource: ERC20Resource) : MarketProvider<PoolingMarket>() {

    suspend fun getToken(address: String): TokenInformationVO {
        return erc20Resource.getTokenInformation(getNetwork(), address)
    }

    fun defaultBalanceFetcher(lpAddress: String): PositionFetcher {
        return PositionFetcher(
            lpAddress,
            { user ->
                erc20Resource.balanceOfFunction(lpAddress, user, getNetwork())
            },
            { retVal ->
                retVal[0].value as BigInteger
            }
        )
    }
}