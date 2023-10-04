package io.defitrack.protocol.stargate

import org.springframework.stereotype.Component

@Component
class StargatePolygonService : StargateService {

    override fun getLpFarm(): String {
        return "0x8731d54E9D02c286767d56ac03e8037C07e01e98"
    }

    override fun getPoolFactory(): String {
        return "0x808d7c71ad2ba3FA531b068a2417C63106BC0949"
    }
}