package io.defitrack.protocol.lido

import org.springframework.stereotype.Service

@Service
class LidoService {

    fun steth(): String {
        return "0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0"
    }

    fun wsteth(): String {
        return "0x7f39c581f595b53c5cb19bd0b3f8da6c935e2ca0"
    }

    fun stMatic(): String {
        return "0x9ee91f9f426fa633d227f7a9b000e28b9dfd8599"
    }
}