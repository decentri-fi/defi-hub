package io.defitrack.token

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.protocol.Protocol
import java.math.BigInteger

data class TokenInformation(
    val network: Network,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: Refreshable<BigInteger> = refreshable(BigInteger.ZERO),
    val underlyingTokens: List<TokenInformation> = emptyList(),
    val protocol: Protocol? = null,
    val verified: Boolean = false,
) {
    fun toFungibleToken(): FungibleToken {
        return FungibleToken(
            address,
            name,
            decimals,
            symbol,
            logo,
            type.name,
            totalSupply.get(),
        )
    }

    suspend fun refresh() {
        totalSupply.refresh()
        this.underlyingTokens.forEach {
            it.refresh()
        }
    }
}