package io.defitrack.price.coingecko

class CoingeckoToken(
    val id: String,
    val symbol: String,
    val name: String,
    val platforms: Map<String, String>
)
