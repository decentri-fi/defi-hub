package io.defitrack.erc20.application

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component
import org.web3j.crypto.Keys

@Component
class LogoGenerator {

    fun generateLogoUrl(network: Network, address: String): String = if (address == "0x0") {
        "https://github.com/defitrack/data/raw/master/logo/network/${network.logo}"
    } else {
        "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/assets/${
            Keys.toChecksumAddress(
                address
            )
        }/logo.png"
    }
}