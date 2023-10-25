package io.defitrack.erc20

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class TokenInformationVO(
    val network: NetworkVO,
    logo: String? = null,
    name: String,
    symbol: String,
    address: String,
    decimals: Int,
    type: TokenType,
    totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<TokenInformationVO> = emptyList(),
    val protocol: ProtocolVO? = null
) : FungibleToken(
    address,
    name,
    decimals,
    symbol,
    logo,
    type.name,
    totalSupply
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }

    fun toFungibleToken(): FungibleToken {
        return FungibleToken(
            address,
            name,
            decimals,
            symbol,
            logo,
            type,
            totalSupply
        )
    }
}