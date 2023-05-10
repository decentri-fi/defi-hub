package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class KyberDMMTokenIdentifier(
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.KYBER_SWAP, TokenType.KYBER, lpContractReader,
) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("DMM-LP")
    }
}