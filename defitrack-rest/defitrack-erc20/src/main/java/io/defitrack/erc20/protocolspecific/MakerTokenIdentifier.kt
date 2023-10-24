package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.erc20.logo.LogoService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class MakerTokenIdentifier(
    private val logoService: LogoService,
    private val erC20ContractReader: ERC20ContractReader
) : TokenIdentifier() {
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
            totalSupply = Refreshable.refreshable(token.totalSupply) {
                erC20ContractReader.getERC20(token.network, token.address).totalSupply
            },
            type = TokenType.SINGLE,
            network = token.network
        )
    }
}