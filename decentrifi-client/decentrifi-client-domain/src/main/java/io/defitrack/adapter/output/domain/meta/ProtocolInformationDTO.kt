package io.defitrack.adapter.output.domain.meta

import io.defitrack.adapter.output.domain.market.DefiPrimitive

data class ProtocolInformationDTO(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val company: CompanyInformationDTO
)