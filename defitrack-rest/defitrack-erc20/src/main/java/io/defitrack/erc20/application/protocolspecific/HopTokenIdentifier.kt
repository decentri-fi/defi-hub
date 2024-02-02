package io.defitrack.erc20.application.protocolspecific

import arrow.core.nonEmptyListOf
import io.defitrack.erc20.ERC20
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.erc20.port.output.ReadERC20Port
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class HopTokenIdentifier(
    private val erc20ContractReader: ReadERC20Port,
    private val hopService: HopService,
) : TokenIdentifier() {

    override suspend fun getTokenInfo(
        token: ERC20,
    ): TokenInformation {
        return hopService.getLps(token.network).find {
            it.lpToken.lowercase() == token.address.lowercase()
        }!!.let { hopLpToken ->

            val saddleToken = erc20ContractReader.getERC20(token.network, token.address)

            val token0 = erc20TokenService.getTokenInformation(hopLpToken.canonicalToken, token.network)
            val token1 = erc20TokenService.getTokenInformation(hopLpToken.hToken, token.network)

            if (saddleToken.isNone() || token0.isNone() || token1.isNone()) {
                throw IllegalStateException("SaddleToken, Token0 or Token1 is not an erc20 for ${token.address}")
            }

            val saddle = saddleToken.getOrNull()!!

            TokenInformation(
                name = saddle.name,
                symbol = saddle.symbol,
                address = token.address.lowercase(),
                decimals = saddle.decimals,
                totalSupply = saddle.totalSupply,
                type = TokenType.CUSTOM_LP,
                protocol = Protocol.HOP,
                underlyingTokens = nonEmptyListOf(token0, token1).map { it.getOrNull()!! },
                network = token.network
            )
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("HOP-LP")
    }
}