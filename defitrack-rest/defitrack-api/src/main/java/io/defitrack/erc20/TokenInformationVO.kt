package io.defitrack.erc20

import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigInteger

class TokenInformationVO(
    val network: NetworkVO,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<TokenInformationVO> = emptyList(),
    val protocol: ProtocolVO? = null
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