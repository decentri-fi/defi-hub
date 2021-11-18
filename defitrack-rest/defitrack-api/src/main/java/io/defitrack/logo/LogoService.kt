package io.defitrack.logo

import io.defitrack.erc20.ERC20Repository
import io.defitrack.common.network.Network
import org.springframework.stereotype.Service
import org.web3j.crypto.Keys

@Service
class LogoService(private val erC20Repository: io.defitrack.erc20.ERC20Repository) {

    fun generateLogoUrl(network: Network, address: String): String {
        return if (address == "0x0") {
            "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/info/logo.png"
        } else {
            erC20Repository.getToken(network, address)?.logo
                ?: "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/assets/${
                    Keys.toChecksumAddress(
                        address
                    )
                }/logo.png"
        }
    }
}