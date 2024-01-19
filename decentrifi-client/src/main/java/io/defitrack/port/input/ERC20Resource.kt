package io.defitrack.port.input

import io.defitrack.common.network.Network
import io.defitrack.domain.FungibleToken
import io.defitrack.domain.WrappedToken
import java.math.BigInteger

interface ERC20Resource {
    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleToken>
    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger
    suspend fun getTokenInformation(network: Network, address: String): FungibleToken
    suspend fun getWrappedToken(network: Network): WrappedToken
    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger
}