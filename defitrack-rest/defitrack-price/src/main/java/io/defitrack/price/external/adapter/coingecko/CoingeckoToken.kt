package io.defitrack.price.external.adapter.coingecko

class CoingeckoToken(
    val id: String,
    val symbol: String,
    val name: String,
    val platforms: Map<String, String>
)
