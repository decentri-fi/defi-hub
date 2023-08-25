package io.defitrack.arbitrum.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BaseContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.base.endpoint:http://defitrack-base:8080}") private val endpoint: String,
) {

    @Bean
    fun baseContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.ARBITRUM,
            "0x9036f1834e18adcdfa075b41aae52cc2a5486574",
            httpClient,
            endpoint
        )
    }
}