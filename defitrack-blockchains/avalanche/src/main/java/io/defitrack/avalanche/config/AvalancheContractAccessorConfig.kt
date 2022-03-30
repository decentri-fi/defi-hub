package io.defitrack.avalanche.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AvalancheContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.avalanche.endpoint:http://defitrack-avalanche:8080}") private val endpoint: String,
) {


    @Bean
    fun avalancheContractAccessor(): EvmContractAccessor {
        return EvmContractAccessor(
            abiDecoder,
            Network.AVALANCHE,
            "0x6FfF95AC47b586bDDEea244b3c2fe9c4B07b9F76",
            httpClient,
            endpoint
        )
    }

}