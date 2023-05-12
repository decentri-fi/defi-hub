package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.CompanyProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
abstract class ProtocolApplication {

    @Bean
    fun provideCompany(): CompanyProvider {
        return object : CompanyProvider {
            override fun getCompany(): Company {
                return this@ProtocolApplication.getCompany()
            }
        }
    }

    abstract fun getCompany(): Company

}
