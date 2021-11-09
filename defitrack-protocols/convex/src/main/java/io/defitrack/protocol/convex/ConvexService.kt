package io.defitrack.protocol.convex

import io.defitrack.ethereum.config.EthereumContractAccessor
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class ConvexService(
    private val ethereumContractAccessor: EthereumContractAccessor,
) {

    @Bean
    fun providePools(): List<ConvexPool> {
        return listOf(
            ConvexPool(address = "0x3Fe65692bfCD0e6CF84cB1E7d24108E434A7587e", name = "cvxCRV Rewards"),
            ConvexPool(address = "0xCF50b810E57Ac33B91dCF525C6ddd9881B139332", name = "CVX Rewards")
        )
    }
}