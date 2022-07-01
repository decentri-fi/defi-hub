package io.defitrack.token

import io.defitrack.common.network.Network
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
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<TokenInformation> = emptyList(),
    val protocol: Protocol? = null
) {
    fun toFungibleToken(): FungibleToken {
        return FungibleToken(
            address,
            name,
            decimals,
            symbol,
            logo,
            type,
        )
    }
}