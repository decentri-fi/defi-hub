package io.defitrack.protocol

enum class Protocol(
    val logo: String,
    val slug: String,
    val dedicatedMicroService: Boolean = true
) {

    AAVE("aave.png", "aave", true),
    CURVE("curve.png", "curve"),
    MSTABLE("mstable.png", "mstable", true),
    COMPOUND("compound.png", "compound", true),
    BEEFY("beefy.png", "beefy", true),
    QUICKSWAP("quickswap.png", "quickswap", true),
    POLYCAT("polycat.webp", "polycat"),
    DINOSWAP("dinoswap.png", "dinoswap"),
    ADAMANT("adamant.png", "adamant", true),
    UNISWAP("uniswap.png", "uniswap", true),
    DFYN("dfyn.svg", "dfyn", true),
    IDEX("idex.png", "idex", true),
    SUSHISWAP("sushiswap.png", "sushiswap", true),
    DMM("dmm.png", "dmm", true),
    BALANCER("balancer.png", "balancer", true),
    CONVEX("convex.png", "convex");

    val imageBasePath = "https://static.defitrack.io/images/protocols/"

    fun getImage(): String = imageBasePath + logo
}