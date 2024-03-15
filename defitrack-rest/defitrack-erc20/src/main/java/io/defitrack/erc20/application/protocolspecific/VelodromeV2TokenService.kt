package io.defitrack.erc20.application.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.VelodromeOptimismService
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import org.springframework.stereotype.Component

@Component
class VelodromeV2TokenService(
    private val velodromeOptimismService: VelodromeOptimismService
) : DefaultLpIdentifier(Protocol.VELODROME_V2) {

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