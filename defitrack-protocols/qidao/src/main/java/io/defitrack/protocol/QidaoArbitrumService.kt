package io.defitrack.protocol

import org.springframework.stereotype.Service

@Service
class QidaoArbitrumService {

    fun provideVaults(): List<String> {
        return listOf(
            "0xc76a3cbefe490ae4450b2fcc2c38666aa99f7aa0",
            "0xb237f4264938f0903f5ec120bb1aa4bee3562fff"
        )
    }

}