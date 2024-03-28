package io.defitrack.balance.service.dto

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.CompanyInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.company.CompanyVO
import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.network.toVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.TokenType

fun FungibleTokenInformation.toVO(): FungibleTokenInformationVO {
    return FungibleTokenInformationVO(
        logo = logo,
        name = name,
        symbol = symbol,
        address = address,
        decimals = decimals,
        type = TokenType.SINGLE,
        totalSupply = totalSupply,
        underlyingTokens = underlyingTokens.map {
            it.toVO()
        },
        protocol = protocol?.toVO(),
        network = network.toNetwork().toVO(),
        verified = verified ?: false
    )
}

fun ProtocolInformationDTO.toVO(): ProtocolVO {
    return ProtocolVO(
        name = name,
        logo = logo,
        slug = slug,
        primitives = primitives.map { DefiPrimitive.valueOf(it.name) },
        company = company.toVO(),
        website = website
    )
}

fun CompanyInformationDTO.toVO(): CompanyVO {
    return CompanyVO(
        name = name,
        slug = slug
    )
}