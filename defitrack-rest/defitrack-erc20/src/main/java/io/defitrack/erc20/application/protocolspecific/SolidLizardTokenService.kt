package io.defitrack.erc20.application.protocolspecific

import io.defitrack.LazyValue
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import org.springframework.stereotype.Service

@Service
class SolidLizardTokenService : DefaultLpIdentifier(Protocol.SOLIDLIZARD) {

    val arbitrumPools = LazyValue {
        val pairFactoryContract = PairFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            contractAddress = "0x734d84631f00dc0d3fcd18b04b6cf42bfd407074"
        )
        pairFactoryContract.allPairs()
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.ARBITRUM -> arbitrumPools.get().contains(token.address)
            else -> false
        }
    }
}