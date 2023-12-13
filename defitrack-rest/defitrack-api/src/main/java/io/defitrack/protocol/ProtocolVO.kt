package io.defitrack.protocol


data class ProtocolVO(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val company: CompanyVO
)