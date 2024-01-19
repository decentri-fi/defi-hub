package io.defitrack.adapter.output.resource

import io.defitrack.domain.CompanyInformation

internal data class CompanyInformationResponse(
    val name: String,
    val slug: String
) {

    fun toCompanyInformation(): CompanyInformation {
        return CompanyInformation(
            name = name,
            slug = slug
        )
    }
}