package io.defitrack.port.output

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.erc20.WrappedTokenDTO
import io.defitrack.common.network.Network
import java.math.BigInteger

interface ERC20Client {

    suspend fun getAllTokens(network: Network, verified: Boolean?): List<FungibleTokenInformation>
    suspend fun getTokenInformation(network: Network, address: String): FungibleTokenInformation
    suspend fun getWrappedToken(network: Network): WrappedTokenDTO
    suspend fun getAllowance(network: Network, token: String, owner: String, spender: String): BigInteger
}
