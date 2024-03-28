package io.defitrack.company

import io.defitrack.protocol.Company

interface CompaniesProvider {
    fun getCompanies(): List<Company>
}