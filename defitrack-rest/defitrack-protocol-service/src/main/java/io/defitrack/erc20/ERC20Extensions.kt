package io.defitrack.erc20

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.CompanyInformationDTO
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.company.CompanyVO
import io.defitrack.network.NetworkInformationVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.ProtocolVO

fun FungibleTokenInformation.toVO(): FungibleTokenInformationVO {
    return FungibleTokenInformationVO(
        network = network.toVO(),
        logo = logo,
        name = name,
        symbol = symbol,
        address = address,
        decimals = decimals,
        type = type,
        totalSupply = totalSupply,
        underlyingTokens = underlyingTokens.map { it.toVO() },
        protocol = protocol?.toVO(),
        verified = verified
    )
}

fun ProtocolInformationDTO.toVO(): ProtocolVO {
    return ProtocolVO(
        name = name,
        logo = logo,
        slug = slug,
        primitives = primitives.map { DefiPrimitive.valueOf(it.name) },
        website = website,
        company = company.toVO()
    )
}

fun CompanyInformationDTO.toVO(): CompanyVO {
    return CompanyVO(
        name = name,
        slug = slug,
    )
}

fun NetworkInformationDTO.toVO(): NetworkInformationVO {
    return NetworkInformationVO(
        name = name,
        logo = logo,
        chainId = chainId,
    )
}