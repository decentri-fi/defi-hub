package io.defitrack.port.output

import io.defitrack.adapter.output.resource.FungibleTokenResponse
import io.defitrack.adapter.output.resource.WrappedTokenResponse
import io.defitrack.common.network.Network
import java.math.BigInteger

internal interface ERC20s {
    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenResponse>
    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger
    suspend fun getTokenInformation(network: Network, address: String): FungibleTokenResponse
    suspend fun getWrappedToken(network: Network): WrappedTokenResponse
    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger
}