package io.defitrack.ethereum.config


import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EthereumContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.ethereum.endpoint:http://defitrack-ethereum.default.svc.cluster.local:8080}") private val endpoint: String
) {

    @Bean
    fun ethereumContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            Network.ETHEREUM,
            MultiCallV2Caller("0x8896D23AfEA159a5e9b72C9Eb3DC4E2684A38EA3"),
            httpClient,
            endpoint
        )
    }
}