package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class KyberDMMTokenIdentifier : DefaultLpIdentifier(Protocol.KYBER_SWAP) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("DMM-LP")
    }
}