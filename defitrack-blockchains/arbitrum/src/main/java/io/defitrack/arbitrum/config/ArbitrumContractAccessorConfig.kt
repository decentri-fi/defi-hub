package io.defitrack.arbitrum.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.multicall.MultiCallV1Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArbitrumContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.arbitrum.endpoint:http://defitrack-arbitrum.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun arbitrumContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.ARBITRUM,
            MultiCallV1Caller("0x2d7aca3bD909bc5DC6DC70894669Adfb6483Bf5F"),
            httpClient,
            endpoint
        )
    }
}