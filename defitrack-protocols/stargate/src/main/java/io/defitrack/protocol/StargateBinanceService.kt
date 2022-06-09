package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateBinanceService : StargateService {
    override fun getLpFarm(): String {
        return "0x3052A0F6ab15b4AE1df39962d5DdEFacA86DaB47"
    }
}