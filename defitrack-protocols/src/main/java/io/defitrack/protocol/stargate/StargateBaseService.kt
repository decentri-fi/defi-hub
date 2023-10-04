package io.defitrack.protocol.stargate

import org.springframework.stereotype.Component

@Component
class StargateBaseService : StargateService {

    override fun getLpFarm(): String {
        return "0x06eb48763f117c7be887296cdcdfad2e4092739c"
    }

    override fun getPoolFactory(): String {
        return "0xaf5191b0de278c7286d6c7cc6ab6bb8a73ba2cd6"
    }
}