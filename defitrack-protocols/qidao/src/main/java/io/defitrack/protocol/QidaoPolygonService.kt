package io.defitrack.protocol

import org.springframework.stereotype.Service

@Service
class QidaoPolygonService {

    fun farms(): List<String> {
        return listOf(
            "0x0635af5ab29fc7bba007b8cebad27b7a3d3d1958",
            "0x574fe4e8120c4da1741b5fd45584de7a5b521f0f",
            "0x07ca17da3b54683f004d388f206269ef128c2356"
        )
    }
}