package io.defitrack.erc20.protocolspecific

import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.erc20.logo.LogoService
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class PoolTogetherTokenIdentifier(
    private val logoService: LogoService,
) : TokenIdentifier() {

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return TokenInformation(
            logo = logoService.generateLogoUrl(token.network, token.address),
            name = token.name,
            symbol = token.symbol,
            address = token.address,
            decimals = token.decimals,
            totalSupply = token.totalSupply,
            type = TokenType.POOLTOGETHER,
            network = token.network
        )
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.name.startsWith("PoolTogether")
    }
}