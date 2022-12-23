package io.defitrack.protocol.wepiggy

import org.springframework.stereotype.Service

@Service
class WepiggyPolygonService {
    fun getComptroller(): String {
        return "0xffceacfd39117030314a07b2c86da36e51787948"
    }
}