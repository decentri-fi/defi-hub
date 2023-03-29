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
    fun polygonZkevmGateway(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder, Network.POLYGON_ZKEVM, "0xfB4C2947223ED76452Ce43D5afda2bcc90D42545", httpClient, endpoint
        )
    }
}