package io.defitrack.protocol.olympusdao

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Service

@Service
class OlympusEthereumService {

    context(BlockchainGateway)
    suspend fun getGOHMContract(): GOHMContract {
        val staking = OlympusStakingContract("0xb63cac384247597756545b500253ff8e607a8020")
        return GOHMContract(
            staking.gOHM()
        )
    }
}