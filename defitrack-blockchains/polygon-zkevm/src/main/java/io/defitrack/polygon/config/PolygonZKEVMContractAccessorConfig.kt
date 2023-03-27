package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonZKEVMContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon-zkevm.endpoint:http://defitrack-polygon-zkevm:8080}") private val endpoint: String,
) {

    @Bean
    fun polygonGateway(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder, Network.POLYGON, "0x11ce4B23bD875D7F5C6a31084f55fDe1e9A87507", httpClient, endpoint
        )
    }
}