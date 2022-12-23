package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Service

@Service
class OlympusEthereumService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    suspend fun getGOHMContract(): GOHMContract {
        val gateway = blockchainGatewayProvider.getGateway(Network.ETHEREUM)
        val staking = OlympusStakingContract(gateway, "0xb63cac384247597756545b500253ff8e607a8020")
        return GOHMContract(
            gateway, staking.gOHM(
            )
        )
    }

}