package io.defitrack.polygonmumbai.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PolygonMumbaiContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon-mumbai.endpoint:http://defitrack-polygon-mumbai:8080}") private val endpoint: String,
) {

    @Bean
    fun polygonMumbaiContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.POLYGON_MUMBAI,
            "0xb976685f95681bb0bd9af04bb2a381d90ac23704",
            httpClient,
            endpoint
        )
    }
}