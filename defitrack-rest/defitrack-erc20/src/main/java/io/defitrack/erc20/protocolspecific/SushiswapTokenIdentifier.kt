package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SushiswapTokenIdentifier(
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.SUSHISWAP, TokenType.SUSHISWAP, lpContractReader,
) {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol == "SLP" && token.name == "SushiSwap LP Token"
    }
}