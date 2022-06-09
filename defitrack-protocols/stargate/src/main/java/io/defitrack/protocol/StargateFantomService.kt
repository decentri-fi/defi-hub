package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateFantomService : StargateService{

    override fun getLpFarm(): String {
        return "0x224D8Fd7aB6AD4c6eb4611Ce56EF35Dec2277F03"
    }
}