package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SpookyTokenIdentifier(
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.SPOOKY, TokenType.SPOOKY, lpContractReader,
) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol == "spLP"
    }
}