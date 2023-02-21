package io.defitrack.protocol.set

import io.defitrack.common.network.Network
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class PolygonSetProvider(
    httpClient: HttpClient
) : AbstractSetProvider(
    Network.POLYGON,
    "https://raw.githubusercontent.com/defitrack/data/master/protocols/set/set.polygon.tokenlist.json",
    httpClient
)