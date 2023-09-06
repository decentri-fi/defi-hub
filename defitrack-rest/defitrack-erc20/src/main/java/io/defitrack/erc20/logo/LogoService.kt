package io.defitrack.erc20.logo

import io.defitrack.common.network.Network
import org.springframework.stereotype.Service
import org.web3j.crypto.Keys

@Service
class LogoService {

    fun generateLogoUrl(network: Network, address: String): String = if (address == "0x0") {
        "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/info/logo.png"
    } else {
           "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/${network.slug}/assets/${
                Keys.toChecksumAddress(
                    address
                )
            }/logo.png"
    }
}