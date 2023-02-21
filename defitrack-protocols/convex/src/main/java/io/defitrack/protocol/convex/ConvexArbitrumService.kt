package io.defitrack.protocol.convex

import org.springframework.stereotype.Service

@Service
class ConvexArbitrumService {
    fun providePools(): List<String> {
        return listOf(
            "0x63f00f688086f0109d586501e783e33f2c950e78",
            "0x90927a78ad13c0ec9acf546ce0c16248a7e7a86d",
            "0xc501491b0e4a73b2efbac564a412a927d2fc83dd",
            "0x8ec22ec81e740e0f9310e7318d03c494e62a70cd",
        )
    }
}