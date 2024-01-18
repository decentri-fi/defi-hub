package io.defitrack.token

import io.defitrack.common.network.Network
import java.math.BigInteger

interface ERC20Resource {
    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleToken>
    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger
    suspend fun getTokenInformation(network: Network, address: String): FungibleToken
    suspend fun getWrappedToken(network: Network): WrappedTokenDTO
    suspend fun getBalancesFor(
        address: String,
        tokens: List<String>,
        network: Network,
    ): List<BigInteger>

    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger
}