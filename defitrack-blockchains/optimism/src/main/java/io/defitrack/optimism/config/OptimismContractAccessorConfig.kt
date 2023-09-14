package io.defitrack.optimism.config

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.multicall.MultiCallV2Caller
import io.ktor.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OptimismContractAccessorConfig(
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.optimism.endpoint:http://defitrack-optimism.default.svc.cluster.local:8080}") private val endpoint: String,
) {

    @Bean
    fun optimismContractAccessor(): BlockchainGateway {
        return BlockchainGateway(
            Network.OPTIMISM,
            MultiCallV2Caller("0x054FfF7ee30953DdB739458e11EAAd51224343a1"),
            httpClient,
            endpoint
        )
    }
}