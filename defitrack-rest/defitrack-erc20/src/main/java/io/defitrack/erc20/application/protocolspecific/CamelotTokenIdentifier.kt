package io.defitrack.erc20.application.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class CamelotTokenIdentifier : DefaultLpIdentifier(Protocol.CAMELOT) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.network == Network.ARBITRUM && token.symbol.startsWith("CMLT-LP")
    }
}