package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class VelodromeV1TokenService(
    private val velodromeOptimismService: VelodromeOptimismService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.VELODROME_V1, TokenType.VELODROME, lpContractReader
) {

    val optimismPools by lazy {
        runBlocking {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = blockchainGatewayProvider.getGateway(Network.OPTIMISM),
                contractAddress = velodromeOptimismService.getV1PoolFactory()
            )
            pairFactoryContract.allPairs()
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.OPTIMISM -> optimismPools.contains(token.address)
            else -> false
        }
    }
}