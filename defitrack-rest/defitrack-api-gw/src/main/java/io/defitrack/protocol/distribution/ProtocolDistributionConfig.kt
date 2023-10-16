package io.defitrack.protocol.distribution

import io.defitrack.protocol.Company
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.IllegalArgumentException

@Configuration
@ConfigurationProperties(prefix = "decentrifi.protocol")
class ProtocolDistributionConfig(
    private var configs: List<Node> = emptyList()
) {

    fun setConfigs(nodes: List<Node>) {
        val includedCompanies = nodes.flatMap { it.companies }
        val notIncludedCompanies = (Company.entries.filter {
            !includedCompanies.contains(it)
        })

        if (notIncludedCompanies.isNotEmpty()) {
            throw IllegalArgumentException(
                """${notIncludedCompanies.joinToString(",")} are not included in distribution config"""
            )
        }
        this.configs = nodes
    }

    fun getConfigs(): List<Node> {
        return configs
    }
}