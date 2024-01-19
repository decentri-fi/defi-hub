package io.defitrack.domain

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleToken(
    val network: NetworkInformation,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleToken> = emptyList(),
    val protocol: ProtocolInformation? = null
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }
}