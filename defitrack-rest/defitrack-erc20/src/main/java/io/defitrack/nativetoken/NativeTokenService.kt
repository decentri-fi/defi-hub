package io.defitrack.nativetoken

import io.defitrack.common.network.Network
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class NativeTokenService {

    companion object {
        val nullAddress = "0x0"
    }

    fun getNativeToken(network: Network): TokenInformation {
        return when (network) {
            Network.ETHEREUM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE
            )
            Network.ARBITRUM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE
            )
            Network.POLYGON -> TokenInformation(
                address = nullAddress,
                name = "Matic",
                decimals = 18,
                symbol = "MATIC",
                type = TokenType.NATIVE
            )
            Network.FANTOM -> TokenInformation(
                address = nullAddress,
                name = "Fantom",
                decimals = 18,
                symbol = "FTM",
                type = TokenType.NATIVE
            )
            Network.OPTIMISM -> TokenInformation(
                address = nullAddress,
                name = "Eth",
                decimals = 18,
                symbol = "ETH",
                type = TokenType.NATIVE
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
                type = TokenType.NATIVE
            )
            Network.POLYGON_MUMBAI -> TokenInformation(
                address = nullAddress,
                name = "Matic",
                decimals = 18,
                symbol = "MATIC",
                type = TokenType.NATIVE
            )
            else -> throw java.lang.IllegalArgumentException("native token for $network not supported")
        }
    }
}