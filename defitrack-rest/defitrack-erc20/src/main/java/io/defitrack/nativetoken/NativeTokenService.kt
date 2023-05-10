package io.defitrack.nativetoken

import io.defitrack.common.network.Network
import io.defitrack.logo.LogoService
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class NativeTokenService(private val logoService: LogoService) {

    companion object {
        val nullAddress = "0x0"
    }

    fun getNativeToken(network: Network): TokenInformation {
        val nativeLogo = logoService.generateLogoUrl(network, "0x0")
        return when (network) {
            Network.ETHEREUM -> TokenInformation(
                address = nullAddress,
                name = "ETH",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            Network.ARBITRUM -> TokenInformation(
                address = nullAddress,
                name = "ETH",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            Network.POLYGON -> TokenInformation(
                address = nullAddress,
                name = "MATIC",
                decimals = 18,
                symbol = "MATIC",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            Network.FANTOM -> TokenInformation(
                address = nullAddress,
                name = "Fantom",
                decimals = 18,
                symbol = "FTM",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            Network.OPTIMISM -> TokenInformation(
                address = nullAddress,
                name = "ETH",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            Network.BINANCE -> TokenInformation(
                address = nullAddress,
                name = "BNB",
                decimals = 18,
                symbol = "BNB",
                type = TokenType.NATIVE,
                logo = nativeLogo,
                network = network
            )
            else -> throw java.lang.IllegalArgumentException("native token for $network not supported")
        }
    }
}