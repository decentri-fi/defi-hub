package io.defitrack.erc20.application.protocolspecific

import io.defitrack.LazyValue
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.erc20.ERC20
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.VelodromeOptimismService
import io.defitrack.uniswap.v2.PairFactoryContract
import org.springframework.stereotype.Component

@Component
class VelodromeV1TokenService(
    private val velodromeOptimismService: VelodromeOptimismService,
) : DefaultLpIdentifier(Protocol.VELODROME_V1) {

    val optimismPools = LazyValue {
        with(getBlockchainGateway()) {
            PairFactoryContract(velodromeOptimismService.getV1PoolFactory())
        }.allPairs()
    }

    private fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(Network.OPTIMISM)
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.OPTIMISM -> optimismPools.get().contains(token.address)
            else -> false
        }
    }
}