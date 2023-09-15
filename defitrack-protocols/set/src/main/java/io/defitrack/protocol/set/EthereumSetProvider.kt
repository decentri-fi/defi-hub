package io.defitrack.protocol.set

import io.defitrack.common.network.Network
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class EthereumSetProvider(
    httpClient: HttpClient
): AbstractSetProvider(
    Network.ETHEREUM,
    "https://raw.githubusercontent.com/decentri-fi/data/master/protocols/set/set.featured.tokenlist.json",
    httpClient
)