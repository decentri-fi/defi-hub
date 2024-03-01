package io.defitrack.protocol.equalizer

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Component

@Component
class EqualizerService(
    blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val voter = lazyAsync {
        with(blockchainGatewayProvider.getGateway(Network.BASE)) {
            EqualizerVoter(
                "0x46abb88ae1f2a35ea559925d99fdc5441b592687"
            )
        }
    }

    val pools = lazyAsync {
        voter.await().pools()
    }

    val gauges = lazyAsync {
        voter.await().gauges(pools.await())
    }
}