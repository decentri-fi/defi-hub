package io.defitrack.erc20.application.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class ApeswapTokenIdentifier : DefaultLpIdentifier(Protocol.APESWAP) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("APE-LP")
    }
}