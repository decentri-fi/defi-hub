package io.defitrack.common.network

enum class Network(val logo: String, val chainId: Int, val slug: String) {
    ETHEREUM("ethereum.png", 1, "ethereum"),
    OPTIMISM("optimism.png", 10, "optimism"),
    ARBITRUM("arbitrum.png", 42161, "arbitrum"),
    FANTOM("fantom.png", 250, "fantom"),
    AVALANCHE("avalanche.png", 43114, "avalanche"),
    BSC("bsc.svg", 56, "bsc"),
    POLYGON("polygon.png", 137, "polygon"),
    POLYGON_MUMBAI("polygon.png", 80001, "polygon_mumbai");


    val imageBasePath = "https://static.defitrack.io/images/networks/"

    fun getImage(): String = imageBasePath + logo

    companion object {
        fun fromChainId(chainId: Int): Network? {
            return values().firstOrNull {
                it.chainId == chainId
            }
        }
    }
}