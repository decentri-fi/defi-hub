package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class UniswapTokenIdentifier : DefaultLpIdentifier(Protocol.UNISWAP_V2) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol == "UNI-V2" && token.network != Network.POLYGON
    }
}