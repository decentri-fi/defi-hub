package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class BalancerEthereumPoolingMarketProvider : BalancerPoolingMarketProvider(
    listOf(
        "0x67d27634e44793fe63c467035e31ea8635117cd4", //meta stable pool factory
        "0xdba127fBc23fb20F5929C546af220A991b5C6e01",
        "0xE061bF85648e9FA7b59394668CfEef980aEc4c66",
        "0x67A25ca2350Ebf4a0C475cA74C257C94a373b828",
        "0xf9ac7B9dF2b3454E841110CcE5550bD5AC6f875F",
        "0x5dd94da3644ddd055fcf6b3e1aa310bb7801eb8b", //weighted pool factory,
        "0xc66ba2b6595d3613ccab350c886ace23866ede24", //stable pool factory,
        "0x0b576c1245F479506e7C8bbc4dB4db07C1CD31F9",
        "0xfADa0f4547AB2de89D1304A668C39B3E09Aa7c76",
        "0x5F43FBa61f63Fa6bFF101a0A0458cEA917f6B347",
        "0xBF904F9F340745B4f0c4702c7B6Ab1e808eA6b93",
        "0x4E11AEec21baF1660b1a46472963cB3DA7811C89",
        "0x897888115Ada5773E02aA29F775430BFB5F34c51",
        "0x5F5222Ffa40F2AEd6380D022184D6ea67C776eE0",
        "0x39A79EB449Fc05C92c39aA6f0e9BfaC03BE8dE5B",
        "0x813EE7a840CE909E7Fea2117A44a90b8063bd4fd"
    ),
    "12703126"
) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}