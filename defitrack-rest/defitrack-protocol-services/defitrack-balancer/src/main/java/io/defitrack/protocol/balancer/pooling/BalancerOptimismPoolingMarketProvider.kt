package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerOptimismPoolingMarketProvider : BalancerPoolingMarketProvider(
    listOf(
        "0x7396f99B48e7436b152427bfA3DD6Aa8C7C6d05B",
        "0x1802953277FD955f9a254B80Aa0582f193cF1d77",
        "0x7ADbdabaA80F654568421887c12F09E0C7BD9629",
        "0x4C32a8a8fDa4E24139B51b456B42290f51d6A1c4",
        "0x230a59F4d9ADc147480f03B0D3fFfeCd56c3289a",
        "0x19DFEF0a828EEC0c85FbB335aa65437417390b85"
    ),
    "0"
) {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}