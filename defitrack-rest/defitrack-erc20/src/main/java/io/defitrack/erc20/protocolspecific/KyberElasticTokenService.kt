package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class KyberElasticTokenService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val kyberElasticOptimismPools by lazy {
        runBlocking {
            KyberswapElasticContract(
                blockchainGatewayProvider.getGateway(Network.OPTIMISM),
                "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
            ).allPairs().map(KyberswapElasticContract.PoolInfo::address)
        }
    }

    fun isKyberElasticToken(address: String, network: Network): Boolean {
        return when (network) {
            Network.OPTIMISM -> kyberElasticOptimismPools.contains(address)
            else -> false
        }
    }
}