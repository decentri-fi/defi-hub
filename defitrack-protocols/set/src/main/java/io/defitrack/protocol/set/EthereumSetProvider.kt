package io.defitrack.protocol.set

import org.springframework.stereotype.Component

@Component
class EthereumSetProvider {

    fun getSets(): List<String> {
        return listOf(
            "0xaa6e8127831c9de45ae56bb1b0d4d4da6e5665bd" //eth x2
        )
    }
}