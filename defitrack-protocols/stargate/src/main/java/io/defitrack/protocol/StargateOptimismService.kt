package io.defitrack.protocol

import org.springframework.stereotype.Component

@Component
class StargateOptimismService {

    fun getLpFarm(): String {
        return "0x4a364f8c717cAAD9A442737Eb7b8A55cc6cf18D8"
    }
}