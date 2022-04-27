package io.defitrack.optimism.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OptimismContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.optimism.endpoint:http://defitrack-optimism:8080}") private val endpoint: String,
) {

    @Bean
    fun optimismContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.OPTIMISM,
            "0xaFE0A0302134df664f0EE212609CA8Fb89255BE4",
            httpClient,
            endpoint
        )
    }
}