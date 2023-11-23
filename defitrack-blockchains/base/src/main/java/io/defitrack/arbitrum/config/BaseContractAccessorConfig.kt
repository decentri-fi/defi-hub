package io.defitrack.arbitrum.config

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BaseContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.base.endpoint:http://defitrack-base.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun baseContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            Network.BASE,
            MultiCallV2Caller("0x8Fe37d605Aa5f2fE4220E713d69a82443aF59C28"),
            httpClient,
            endpoint
        )
    }
}