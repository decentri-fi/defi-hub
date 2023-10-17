package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.distribution.ProtocolDistributionConfig
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest
class ApiGwSmokeTest {

    @Autowired
    private lateinit var protocolDistributionConfig: ProtocolDistributionConfig

    @Test
    fun `protocol distribution config should match property file`() {
        val configs = protocolDistributionConfig.getConfigs()
        assertThat(configs.size).isEqualTo(13)
    }

    @Test
    fun `protocol distribution config should contain all companies`() {
        val configuredCompanies = protocolDistributionConfig.getConfigs().flatMap { it.companies }
        assertThat(configuredCompanies).containsAll(Company.entries)
    }
}