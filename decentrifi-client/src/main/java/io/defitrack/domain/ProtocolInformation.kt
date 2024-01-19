package io.defitrack.domain

import io.defitrack.protocol.DefiPrimitive


data class ProtocolInformation(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val company: CompanyInformation
)