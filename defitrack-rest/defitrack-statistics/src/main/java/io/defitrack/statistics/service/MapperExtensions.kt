package io.defitrack.statistics.service

import io.defitrack.adapter.output.domain.meta.CompanyInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.company.CompanyVO
import io.defitrack.protocol.DefiPrimitive
import io.defitrack.protocol.ProtocolVO

fun ProtocolInformationDTO.toProtocolVO(): ProtocolVO {
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