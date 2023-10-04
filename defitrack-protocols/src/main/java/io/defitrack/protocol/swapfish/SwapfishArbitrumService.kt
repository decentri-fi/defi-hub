package io.defitrack.protocol.swapfish

import org.springframework.stereotype.Component

@Component
class SwapfishArbitrumService {

    fun provideMasterchefs(): List<String> {
        return listOf(
            "0x33141e87ad2DFae5FBd12Ed6e61Fa2374aAeD029"
        )
    }
}