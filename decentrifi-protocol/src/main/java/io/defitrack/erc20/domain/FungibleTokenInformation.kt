package io.defitrack.erc20.domain

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.networkinfo.NetworkInformation
import io.defitrack.protocol.ProtocolInformation
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleTokenInformation(
    val network: NetworkInformation,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleTokenInformation> = emptyList(),
    val protocol: ProtocolInformation? = null,
    val verified: Boolean,
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }
}