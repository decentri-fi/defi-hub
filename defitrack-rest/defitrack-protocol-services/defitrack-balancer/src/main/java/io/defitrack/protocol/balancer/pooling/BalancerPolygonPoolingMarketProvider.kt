package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.web3j.abi.TypeEncoder
import org.web3j.abi.datatypes.Address

@Component
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonPoolingMarketProvider : BalancerPoolingMarketProvider(
    listOf(
        "0xAB2372275809E15198A7968C7f324053867cdB0C",
        "0x8e9aa87e45e92bad84d5f8dd1bff34fb92637de9",
        "0x7bc6C0E73EDAa66eF3F6E2f27b0EE8661834c6C9",
        "0x6Ab5549bBd766A43aFb687776ad8466F8b42f777",
        "0x5C5fCf8fBd4cd563cED27e7D066b88ee20E1867A",
        "0xB8Dfa4fd0F083de2B7EDc0D5eeD5E684e54bA45D",
        "0xFc8a407Bba312ac761D8BFe04CE1201904842B76",
        "0x0b576c1245F479506e7C8bbc4dB4db07C1CD31F9"
    ),
    "0"
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}