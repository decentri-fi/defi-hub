package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.contract.KyberswapElasticContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class KyberElasticTokenService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.KYBER_SWAP, TokenType.KYBER_ELASTIC, erC20Resource, lpContractReader
) {

    val kyberElasticOptimismPools by lazy {
        runBlocking {
            KyberswapElasticContract(
                blockchainGatewayProvider.getGateway(Network.OPTIMISM),
                "0xb85ebe2e4ea27526f817ff33fb55fb240057c03f"
            ).allPairs().map(KyberswapElasticContract.PoolInfo::address)
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.OPTIMISM -> kyberElasticOptimismPools.contains(token.address)
            else -> false
        }
    }
}