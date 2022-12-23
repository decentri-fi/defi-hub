package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import java.math.BigInteger

class ERC20(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: Network,
    val address: String,
    val totalSupply: BigInteger,
) {
    fun toToken(): TokenInformation = TokenInformation(
        name = name,
        symbol = symbol,
        decimals = decimals,
        address = address,
        type = TokenType.SINGLE,
        totalSupply = totalSupply,
        network = network
    )
}