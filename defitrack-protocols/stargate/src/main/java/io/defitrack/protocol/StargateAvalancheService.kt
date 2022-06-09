package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateAvalancheService : StargateService{

    override fun getLpFarm(): String {
        return "0x8731d54E9D02c286767d56ac03e8037C07e01e98"
    }
}