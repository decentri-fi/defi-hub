package io.defitrack.protocol.uniswap.v2.pooling.prefetch

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.networkinfo.toNetworkInformation
import io.defitrack.protocol.ProtocolInformation
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleTokenInformation(
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleTokenInformation> = emptyList(),
    val protocol: ProtocolInformation? = null
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }

    fun toFungibleToken(network: Network): io.defitrack.erc20.domain.FungibleTokenInformation {
        return io.defitrack.erc20.domain.FungibleTokenInformation(
            logo = logo,
            name = name,
            symbol = symbol,
            address = address,
            decimals = decimals,
            type = TokenType.SINGLE,
            totalSupply = totalSupply,
            underlyingTokens = underlyingTokens.map {
                it.toFungibleToken(network)
            },
            protocol = protocol,
            network = network.toNetworkInformation()
        )
    }
}