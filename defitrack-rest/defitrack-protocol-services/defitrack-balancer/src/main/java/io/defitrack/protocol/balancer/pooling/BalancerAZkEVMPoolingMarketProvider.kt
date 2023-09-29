package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerAZkEVMPoolingMarketProvider : BalancerPoolingMarketProvider(
    listOf(
        "0x4b7b369989e613ff2C65768B7Cf930cC927F901E",
        "0x8eA89804145c007e7D226001A96955ad53836087",
        "0x6B1Da720Be2D11d95177ccFc40A917c2688f396c",
        "0x687b8C9b41E01Be8B591725fac5d5f52D0564d79",
        "0xaf779e58dafb4307b998C7b3C9D3f788DFc80632",
        "0x03F3Fb107e74F2EAC9358862E91ad3c692712054",
        "0x44d33798dddCdAbc93Fe6a40C80588033Dc502d3"
    ),
    "0"
) {

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}