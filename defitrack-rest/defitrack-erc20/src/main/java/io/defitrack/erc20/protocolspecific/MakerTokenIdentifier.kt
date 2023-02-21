package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.logo.LogoService
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class MakerTokenIdentifier(
    private val logoService: LogoService
) : TokenIdentifier {
    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.network == Network.ETHEREUM && token.address.lowercase() == "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2"
    }

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return TokenInformation(
            logo = logoService.generateLogoUrl(token.network, token.address),
            name = "Maker",
            symbol = "MKR",
            address = token.address,
            decimals = token.decimals,
            totalSupply = token.totalSupply,
            type = TokenType.SINGLE,
            network = token.network
        )
    }
}