package io.defitrack.protocol

import org.springframework.stereotype.Service

@Service
class QidaoArbitrumService {

    fun provideVaults(): List<String> {
        return listOf(
            "0xc76a3cbefe490ae4450b2fcc2c38666aa99f7aa0"
        )
    }

}