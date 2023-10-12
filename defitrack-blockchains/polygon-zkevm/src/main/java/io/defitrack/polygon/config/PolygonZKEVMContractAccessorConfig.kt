package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.multicall.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonZKEVMContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon-zkevm.endpoint:http://defitrack-polygon-zkevm.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun polygonZkevmGateway(): BlockchainGateway {
        return BlockchainGateway(
            Network.POLYGON_ZKEVM,
            MultiCallV2Caller("0x7f593DEdebE173CA12954164dc3db56131FCA0F7"),
            httpClient,
            endpoint
        )
    }
}