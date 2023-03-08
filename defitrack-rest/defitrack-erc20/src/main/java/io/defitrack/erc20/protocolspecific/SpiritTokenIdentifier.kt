package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SpiritTokenIdentifier(
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.SPIRITSWAP, TokenType.SPIRIT, lpContractReader,
) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("SPIRIT-LP")
    }
}