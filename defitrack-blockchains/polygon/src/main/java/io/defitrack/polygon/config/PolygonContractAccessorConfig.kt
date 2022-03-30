package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.ContractInteractionCommand
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.EthCall

@Configuration
class PolygonContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.polygon.endpoint:http://defitrack-polygon:8080}") private val endpoint: String,
) {

     @Bean
    fun polygonContractAccessor(): EvmContractAccessor {
        return EvmContractAccessor(
            abiDecoder,
            Network.POLYGON,
            "0x11ce4B23bD875D7F5C6a31084f55fDe1e9A87507",
            httpClient,
            endpoint
        )
    }
}