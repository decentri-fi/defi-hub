package io.defitrack.erc20.application.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.application.LogoGenerator
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class PoolTogetherTokenIdentifier(private val logoGenerator: LogoGenerator) : TokenIdentifier() {

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return TokenInformation(
            logo = logoGenerator.generateLogoUrl(token.network, token.address),
            name = token.name,
            symbol = token.symbol,
            address = token.address,
            decimals = token.decimals,
            totalSupply = token.totalSupply,
            type = TokenType.CUSTOM_LP,
            network = token.network
        )
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.name.startsWith("PoolTogether")
    }
}