package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class KyberElasticTokenService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.KYBER_SWAP, TokenType.KYBER_ELASTIC, lpContractReader
) {

    val kyberElasticOptimismPools = lazyAsync {
        KyberswapElasticContract(
            blockchainGatewayProvider.getGateway(Network.OPTIMISM),
            "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
        ).allPairs().map(KyberswapElasticContract.PoolInfo::address)
    }


    val kyberElasticPolygonPools = lazyAsync {
        KyberswapElasticContract(
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            "0xbdec4a045446f583dc564c0a227ffd475b329bf0"
        ).allPairs().map(KyberswapElasticContract.PoolInfo::address)
    }

    val kyberElasticEthereumPools = lazyAsync {
        KyberswapElasticContract(
            blockchainGatewayProvider.getGateway(Network.ETHEREUM),
            "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
        ).allPairs().map(KyberswapElasticContract.PoolInfo::address)
    }


    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.OPTIMISM -> kyberElasticOptimismPools.await().contains(token.address)
            Network.POLYGON -> kyberElasticPolygonPools.await().contains(token.address)
            Network.ETHEREUM -> kyberElasticEthereumPools.await().contains(token.address)
            else -> false
        }
    }
}