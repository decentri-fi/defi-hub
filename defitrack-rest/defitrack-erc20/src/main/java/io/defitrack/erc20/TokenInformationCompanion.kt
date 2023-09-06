package io.defitrack.erc20

import io.defitrack.network.toVO
import io.defitrack.token.TokenInformation

fun TokenInformation.toVO(): TokenInformationVO {
    return TokenInformationVO(
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