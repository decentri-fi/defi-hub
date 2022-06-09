package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateEthereumService : StargateService {

    override fun getLpFarm(): String {
        return "0xB0D502E938ed5f4df2E681fE6E419ff29631d62b"
    }
}