package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.token.Token
import io.defitrack.token.TokenType

class ERC20(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: Network,
    val address: String,
) {
    fun toToken(): Token = Token(
        name = name,
        symbol = symbol,
        decimals = decimals,
        address = address,
        type = TokenType.SINGLE,
    )
}