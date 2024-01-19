package io.defitrack.adapter.output.resource

import io.defitrack.domain.ProtocolInformation
import io.defitrack.protocol.DefiPrimitive


internal data class ProtocolInformationResponse(
    val name: String,
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val company: CompanyInformationResponse
) {

    fun toProtocolInformation(): ProtocolInformation {
        return ProtocolInformation(
            name = name,
            logo = logo,
            slug = slug,
            primitives = primitives,
            website = website,
            company = company.toCompanyInformation()
        )
    }
}