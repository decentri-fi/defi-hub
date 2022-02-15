package io.defitrack.protocol.yearn


class YearnV2Vault(
    val id: String,
    val token: YearnV2Token,
    val shareToken: YearnV2Token,
    val apiVersion: String
)

class YearnV2Token(
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int
)

