package io.defitrack

import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
abstract class ProtocolApplication {

    @Bean
    fun provideProtocol(): ProtocolProvider {
        return object : ProtocolProvider {
            override fun getProtocol(): Protocol {
                return getProtocol()
            }
        }
    }

    abstract fun getProtocol(): Protocol

}
