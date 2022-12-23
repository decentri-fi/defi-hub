package io.defitrack.market.pooling

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.invest.MarketProvider
import io.defitrack.market.lending.domain.BalanceFetcher
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.token.ERC20Resource
import java.math.BigInteger

abstract class PoolingMarketProvider(val erc20Resource: ERC20Resource) : MarketProvider<PoolingMarket>() {

    suspend fun getToken(address: String): TokenInformationVO {
        return erc20Resource.getTokenInformation(getNetwork(), address)
    }

    fun defaultBalanceFetcher(lpAddress: String): BalanceFetcher {
        return BalanceFetcher(
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