package io.defitrack.protocol

import org.springframework.stereotype.Service

@Service
class VelodromeOptimismService {

    fun getV1PoolFactory(): String {
        return "0x25cbddb98b35ab1ff77413456b31ec81a6b6b746"
    }

    fun getV2PoolFactory(): String {
        return "0xf1046053aa5682b4f9a81b5481394da16be5ff5a"
    }
}