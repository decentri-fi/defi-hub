package io.defitrack.dracula

import io.defitrack.ethereum.config.EthereumContractAccessor
import org.springframework.stereotype.Service

@Service
class DraculaService {
    fun provideDrcRewardPools(ethereumContractAccessor: EthereumContractAccessor) {
        listOf("0xC8DFD57E82657f1e7EdEc5A9aA4906230C29A62A").map {
            RewardPool(
                address = it
            )
        }
    }
}