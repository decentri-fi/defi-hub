package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateOptimismService : StargateService {

    override fun getLpFarm(): String {
        return "0x4a364f8c717cAAD9A442737Eb7b8A55cc6cf18D8"
    }

    override fun getPoolFactory(): String {
        return "0xE3B53AF74a4BF62Ae5511055290838050bf764Df"
    }
}