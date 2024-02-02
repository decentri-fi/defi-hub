package io.defitrack.erc20.port.`in`

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.domain.WrappedToken
import java.math.BigInteger

interface ERC20Resource {
    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenInformation>
    suspend fun getBalance(network: Network, tokenAddress: String, user: String): BigInteger
    suspend fun getTokenInformation(network: Network, address: String): FungibleTokenInformation
    suspend fun getWrappedToken(network: Network): WrappedToken
    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger
}