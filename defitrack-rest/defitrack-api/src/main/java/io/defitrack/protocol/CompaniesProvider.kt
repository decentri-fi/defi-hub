package io.defitrack.protocol

interface CompaniesProvider {
    fun getCompanies(): List<Company>
}