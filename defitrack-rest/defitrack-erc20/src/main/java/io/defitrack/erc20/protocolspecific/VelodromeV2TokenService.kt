package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.VelodromeOptimismService
import io.defitrack.protocol.contract.PoolFactoryContract
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class VelodromeV2TokenService(
    private val velodromeOptimismService: VelodromeOptimismService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.VELODROME_V2, TokenType.VELODROME, lpContractReader
) {

    val optimismPools = lazyAsync {
        val pairFactoryContract = PoolFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.OPTIMISM),
            contractAddress = velodromeOptimismService.getV2PoolFactory()
        )
        pairFactoryContract.allPools()
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.OPTIMISM -> optimismPools.await().contains(token.address)
            else -> false
        }
    }
}