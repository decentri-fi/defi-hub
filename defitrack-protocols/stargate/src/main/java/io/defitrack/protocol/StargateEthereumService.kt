package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateEthereumService : StargateService {

    override fun getLpFarm(): String {
        return "0xB0D502E938ed5f4df2E681fE6E419ff29631d62b"
    }

    override fun getPoolFactory(): String {
        return "0x06D538690AF257Da524f25D0CD52fD85b1c2173E"
    }
}