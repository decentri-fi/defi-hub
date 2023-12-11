package io.defitrack.erc20

import io.defitrack.network.toVO

fun TokenInformation.toVO(): FungibleToken {
    return FungibleToken(
        network = network.toVO(),
        logo = logo,
        name = name,
        symbol = symbol,
        address = address,
        decimals = decimals,
        type = type,
        totalSupply = totalSupply.get(),
        underlyingTokens = underlyingTokens.map { it.toVO() },
    )
}