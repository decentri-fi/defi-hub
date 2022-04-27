package io.defitrack.ethereum.config


import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EthereumContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.ethereum.endpoint:http://defitrack-ethereum:8080}") private val endpoint: String
) {

    @Bean
    fun ethereumContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            abiDecoder,
            Network.ETHEREUM,
            "0xeefba1e63905ef1d7acba5a8513c70307c1ce441",
            httpClient,
            endpoint
        )
    }
}