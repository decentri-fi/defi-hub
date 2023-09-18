package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.CompanyProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
abstract class ProtocolApplication(applicationContext: ApplicationContext) {

    init {
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.REFUSING_TRAFFIC)
    }


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
