package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import java.math.BigInteger

data class TokenInformation(
    val network: Network,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: Refreshable<BigInteger> = refreshable(BigInteger.ZERO),
    val underlyingTokens: List<TokenInformation> = emptyList(),
    val protocol: Protocol? = null,
    val verified: Boolean = false,
) {

    suspend fun refresh() {
        totalSupply.refresh()
        this.underlyingTokens.forEach {
            it.refresh()
        }
    }
}