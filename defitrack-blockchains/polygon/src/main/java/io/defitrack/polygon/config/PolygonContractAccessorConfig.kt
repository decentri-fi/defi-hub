package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.multicall.MultiCallV1Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon.endpoint:http://defitrack-polygon.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun polygonGateway(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.POLYGON,
            MultiCallV1Caller("0x11ce4B23bD875D7F5C6a31084f55fDe1e9A87507"),
            httpClient,
            endpoint
        )
    }
}