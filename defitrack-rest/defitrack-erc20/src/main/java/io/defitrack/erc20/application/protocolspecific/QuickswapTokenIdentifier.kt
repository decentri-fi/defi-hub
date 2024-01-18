package io.defitrack.erc20.application.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class QuickswapTokenIdentifier : DefaultLpIdentifier(Protocol.QUICKSWAP) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol == "UNI-V2" && token.network == Network.POLYGON
    }
}