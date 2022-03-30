package io.defitrack.fantom.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FantomContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.fantom.endpoint:http://defitrack-fantom:8080}") private val endpoint: String,
) {

    @Bean
    fun fantomContractAccessor(): EvmContractAccessor {
        return EvmContractAccessor(
            abiDecoder,
            Network.FANTOM,
            "0x2d5408f2287bf9f9b05404794459a846651d0a59",
            httpClient,
            endpoint
        )
    }
}