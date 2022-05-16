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
        val nativeLogo = logoService.generateLogoUrl(network)
        return when (network) {
            Network.ETHEREUM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo
            )
            Network.ARBITRUM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo
            )
            Network.POLYGON -> TokenInformation(
                address = nullAddress,
                name = "Matic",
                decimals = 18,
                symbol = "MATIC",
                type = TokenType.NATIVE,
                logo = nativeLogo
            )
            Network.FANTOM -> TokenInformation(
                address = nullAddress,
                name = "Fantom",
                decimals = 18,
                symbol = "FTM",
                type = TokenType.NATIVE,
                logo = nativeLogo,
            )
            Network.OPTIMISM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE,
                logo = nativeLogo,
            )
            Network.AVALANCHE -> TokenInformation(
                address = nullAddress,
                name = "Avalanche",
                decimals = 18,
                symbol = "AVAX",
                type = TokenType.NATIVE
            )
            Network.BSC -> TokenInformation(
                address = nullAddress,
                name = "BNB",
                decimals = 18,
                symbol = "BNB",
                type = TokenType.NATIVE,
                logo = nativeLogo,
            )
            Network.POLYGON_MUMBAI -> TokenInformation(
                address = nullAddress,
                name = "Matic",
                decimals = 18,
                symbol = "MATIC",
                type = TokenType.NATIVE,
                logo = nativeLogo,
            )
            else -> throw java.lang.IllegalArgumentException("native token for $network not supported")
        }
    }
}