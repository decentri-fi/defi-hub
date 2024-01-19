package io.defitrack.protocol.apeswap

import org.springframework.stereotype.Component

@Component
class ApeswapPolygonService {

    fun provideFactory(): String {
        return "0xcf083be4164828f00cae704ec15a36d711491284"
    }
}