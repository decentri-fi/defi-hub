package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class BalancerArbitrumPoolingMarketProvider : BalancerPoolingMarketProvider(
    listOf(
        "0x7396f99B48e7436b152427bfA3DD6Aa8C7C6d05B",
        "0x1c99324edc771c82a0dccb780cc7dda0045e50e7",
        "0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95",
        "0x7ADbdabaA80F654568421887c12F09E0C7BD9629",
        "0x8eA89804145c007e7D226001A96955ad53836087",
        "0xc7E5ED1054A24Ef31D827E6F86caA58B3Bc168d7",
        "0x19DFEF0a828EEC0c85FbB335aa65437417390b85"
    ),
    "0"
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}