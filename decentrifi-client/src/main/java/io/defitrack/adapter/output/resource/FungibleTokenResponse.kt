package io.defitrack.adapter.output.resource

import io.defitrack.domain.FungibleToken
import java.math.BigInteger

internal class FungibleTokenResponse(
    val network: NetworkInformationResponse,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenTypeResponse,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleTokenResponse> = emptyList(),
    val protocol: ProtocolInformationResponse? = null
) {

    fun toFungibleToken(): FungibleToken {
        return FungibleToken(
            network = network.toNetworkInformation(),
            logo = logo,
            name = name,
            symbol = symbol,
            address = address,
            decimals = decimals,
            type = type.toTokenType(),
            totalSupply = totalSupply,
            underlyingTokens = underlyingTokens.map { it.toFungibleToken() },
            protocol = protocol?.toProtocolInformation()
        )
    }

}