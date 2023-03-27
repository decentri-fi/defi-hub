package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateBinanceService : StargateService {
    override fun getLpFarm(): String {
        return "0x3052A0F6ab15b4AE1df39962d5DdEFacA86DaB47"
    }

    override fun getPoolFactory(): String {
        return "0xe7Ec689f432f29383f217e36e680B5C855051f25"
    }

}