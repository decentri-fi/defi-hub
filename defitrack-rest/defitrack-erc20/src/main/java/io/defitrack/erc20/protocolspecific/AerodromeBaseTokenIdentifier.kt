package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class AerodromeBaseTokenIdentifier : DefaultLpIdentifier(Protocol.AERODROME) {

    private val poolFactoryAddress: String = "0x420DD381b31aEf6683db6B902084cB0FFECe40Da"

    val cache = Cache.Builder<String, List<String>>().expireAfterWrite(1.days).build()

    suspend fun getPools() = cache.get("all") {
        PoolFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.BASE),
            contractAddress = poolFactoryAddress
        ).allPools().map(String::lowercase)
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.network == Network.BASE && getPools().contains(token.address.lowercase())
    }
}