package io.defitrack.protocol.set

import io.defitrack.common.network.Network
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class PolygonSetProvider(
    httpClient: HttpClient
) : AbstractSetProvider(
    Network.POLYGON,
    "https://raw.githubusercontent.com/decentri-fi/data/master/protocols/set/set.polygon.tokenlist.json",
    httpClient
) {
    override fun extraSets(): List<String> {
        return listOf(
            "0x3ad707da309f3845cd602059901e39c4dcd66473",
            "0xf287d97b6345bad3d88856b26fb7c0ab3f2c7976"
        )
    }
}