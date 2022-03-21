package io.defitrack.token

import io.defitrack.protocol.Protocol
import java.math.BigInteger

class TokenInformation(
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val tokenInformation0: TokenInformation? = null,
    val tokenInformation1: TokenInformation? = null,
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