package io.defitrack.common.network

enum class Network(val logo: String, val chainId: Int, val slug: String, val hasMicroService: Boolean = true) {
    ETHEREUM("ethereum.png", 1, "ethereum"),
    OPTIMISM("optimism.png", 10, "optimism"),
    ARBITRUM("arbitrum.png", 42161, "arbitrum"),
    FANTOM("fantom.png", 250, "fantom"),
    AVALANCHE("avalanche.png", 43114, "avalanche"),
    BINANCE("bsc.svg", 56, "binance"),
    POLYGON("polygon.png", 137, "polygon"),
    STARKET("starknet.png", 0, "starknet", false);

    val imageBasePath = "https://github.com/defitrack/data/raw/master/logo/network/"

    fun getImage(): String = imageBasePath + logo

    companion object {
        fun fromChainId(chainId: Int): Network? {
            return values().firstOrNull {
                it.chainId == chainId
            }
        }
    }
}