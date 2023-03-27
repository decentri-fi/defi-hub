package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateFantomService : StargateService{

    override fun getLpFarm(): String {
        return "0x224D8Fd7aB6AD4c6eb4611Ce56EF35Dec2277F03"
    }

    override fun getPoolFactory(): String {
        return "0x9d1B1669c73b033DFe47ae5a0164Ab96df25B944"
    }
}