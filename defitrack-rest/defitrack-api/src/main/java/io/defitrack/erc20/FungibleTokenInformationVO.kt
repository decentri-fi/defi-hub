package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleTokenInformationVO(
    val network: NetworkInformationVO,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleTokenInformationVO> = emptyList(),
    val protocol: ProtocolVO? = null,
    val verified: Boolean,
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }
}