package io.defitrack.protocol

import io.defitrack.company.CompanyInformation


data class ProtocolInformation(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val company: CompanyInformation
)