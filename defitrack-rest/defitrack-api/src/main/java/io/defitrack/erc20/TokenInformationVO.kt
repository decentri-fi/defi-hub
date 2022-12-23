package io.defitrack.erc20

import io.defitrack.network.NetworkVO
import io.defitrack.network.toVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.protocol.toVO
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenInformation
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

fun TokenInformation.toVO(): TokenInformationVO {
    return TokenInformationVO(
        network = network.toVO(),
        logo = logo,
        name = name,
        symbol = symbol,
        address = address,
        decimals = decimals,
        type = type,
        totalSupply = totalSupply,
        underlyingTokens = underlyingTokens.map { it.toVO() },
        protocol = protocol?.toVO()
    )
}