package io.defitrack.protocol.compound

import org.springframework.stereotype.Service

@Service
class CompoundEthereumService {
    fun getComptroller(): String {
        return "0x3d9819210a31b4961b30ef54be2aed79b9c9cd3b"
    }

    fun getV3Tokens(): List<String> {
        return listOf(
            "0xc3d688B66703497DAA19211EEdff47f25384cdc3"
        )
    }
}