package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SushiswapTokenIdentifier(
    erC20Resource: ERC20Resource,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.SUSHISWAP, TokenType.SUSHISWAP, erC20Resource, lpContractReader,
) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol == "SLP"
    }
}