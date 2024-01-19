package io.defitrack.protocol.convex

import org.springframework.stereotype.Service

@Service
class ConvexEthereumService {

    fun providePools(): List<String> {
        return listOf(
            "0x3Fe65692bfCD0e6CF84cB1E7d24108E434A7587e",
            "0xCF50b810E57Ac33B91dCF525C6ddd9881B139332",
            "0x0a760466e1b4621579a82a39cb56dda2f4e70f03",
            "0x6b27d7bc63f1999d14ff9ba900069ee516669ee8",
            "0x7e880867363a7e321f5d260cade2b0bb2f717b02"
        )
    }


    fun provideBooster(): String {
        return "0xf403c135812408bfbe8713b5a23a04b3d48aae31"
    }

}