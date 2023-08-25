package io.defitrack.common.network

enum class Network(val logo: String, val chainId: Int, val slug: String, val hasMicroService: Boolean = true) {
    ETHEREUM("ethereum.png", 1, "ethereum"),
    OPTIMISM("optimism.png", 10, "optimism"),
    ARBITRUM("arbitrum.png", 42161, "arbitrum"),
    POLYGON("polygon.png", 137, "polygon"),
    POLYGON_ZKEVM("polygon-zkevm.png", 1101, "polygon-zkevm"),
    BASE("base.png", 8453, "base");

    val imageBasePath = "https://github.com/defitrack/data/raw/master/logo/network/"

    fun getImage(): String = imageBasePath + logo

    companion object {
        fun fromChainId(chainId: Int): Network? {
            return values().firstOrNull {
                it.chainId == chainId
            }
        }

        fun fromString(str: String): Network? {
            return values().firstOrNull {
                it.slug == str || it.name == str
            }
        }
    }
}