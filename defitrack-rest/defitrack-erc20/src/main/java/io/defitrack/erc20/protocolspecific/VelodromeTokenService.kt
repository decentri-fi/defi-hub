package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.PairFactoryContract
import io.defitrack.protocol.VelodromeOptimismService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class VelodromeTokenService(
    private val velodromeOptimismService: VelodromeOptimismService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val optimismPools by lazy {
        runBlocking {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = blockchainGatewayProvider.getGateway(Network.OPTIMISM),
                contractAddress = velodromeOptimismService.getPoolFactory()
            )
            pairFactoryContract.allPairs()
        }
    }

    fun isVelodromeToken(address: String, network: Network): Boolean {
        return when (network) {
            Network.OPTIMISM -> optimismPools.contains(address)
            else -> false
        }
    }
}