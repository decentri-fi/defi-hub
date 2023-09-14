package io.defitrack.arbitrum.config

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.multicall.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArbitrumContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.arbitrum.endpoint:http://defitrack-arbitrum.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun arbitrumContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            Network.ARBITRUM,
            MultiCallV2Caller("0x5B2bAa4d451916cec20a3a4ECe2A6c30c1F09a62"),
            httpClient,
            endpoint
        )
    }
}