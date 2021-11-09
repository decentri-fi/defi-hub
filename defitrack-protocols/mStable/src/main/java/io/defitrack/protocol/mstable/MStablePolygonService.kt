package io.defitrack.mstable

import org.springframework.stereotype.Service

@Service
class MStablePolygonService {

    fun getSavingsContracts(): List<String> {
        return listOf("0x5290Ad3d83476CA6A2b178Cd9727eE1EF72432af")
    }
}