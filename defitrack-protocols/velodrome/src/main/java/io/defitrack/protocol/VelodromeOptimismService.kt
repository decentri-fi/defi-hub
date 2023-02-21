package io.defitrack.protocol

import org.springframework.stereotype.Service

@Service
class VelodromeOptimismService {

    fun getPoolFactory(): String {
        return "0x25cbddb98b35ab1ff77413456b31ec81a6b6b746"
    }
}