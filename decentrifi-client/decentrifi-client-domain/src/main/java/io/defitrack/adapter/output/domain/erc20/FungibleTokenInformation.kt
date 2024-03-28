package io.defitrack.adapter.output.domain.erc20

import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleTokenInformation(
    val network: NetworkInformationDTO,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleTokenInformation> = emptyList(),
    val protocol: ProtocolInformationDTO? = null,
    val verified: Boolean,
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }
}