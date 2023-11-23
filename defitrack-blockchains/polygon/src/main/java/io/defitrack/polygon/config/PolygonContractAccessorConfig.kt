package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon.endpoint:http://defitrack-polygon.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun polygonGateway(): BlockchainGateway {
        return BlockchainGateway(
            Network.POLYGON,
            MultiCallV2Caller("0x4499487181455E46Fc4592a59a93508bd8dB8A6e"),
            httpClient,
            endpoint
        )
    }
}