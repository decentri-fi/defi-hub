package io.defitrack.logo

import io.defitrack.common.network.Network
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import org.springframework.stereotype.Service
import org.web3j.crypto.Keys

@Service
class LogoService(private val erC20Resource: ERC20Resource) {

    fun generateLogoUrl(network: Network, address: String): String {
        return if (address == "0x0") {
            "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/info/logo.png"
        } else {
            getToken(network, address)?.logo
                ?: "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/assets/${
                    Keys.toChecksumAddress(
                        address
                    )
                }/logo.png"
        }
    }

    private fun getToken(
        network: Network,
        address: String
    ): TokenInformation? = try {
        erC20Resource.getTokenInformation(network, address)
    } catch (ex: Exception) {
        null
    }
}
