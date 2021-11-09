package io.codechef.protocol

enum class Protocol(
    val logo: String,
    val slug: String,
    val dedicatedMicroService: Boolean = true
) {

    AAVE("aave.png", "aave", true),
    CURVE("curve.png", "curve"),
    MSTABLE("mstable.png", "mstable", true),
    YEARN("yearn.png", "yearn"),
    COMPOUND("compound.png", "compound", true),
    IRON("iron.png", "iron"),
    BELT("belt.png", "belt"),
    BEEFY("beefy.png", "beefy", true),
    QUICKSWAP("quickswap.png", "quickswap", true),
    DRACULA("dracula.webp", "dracula"),
    POLYCAT("polycat.webp", "polycat"),
    ADAMANT("adamant.png", "adamant", true),
    UNISWAP("uniswap.png", "uniswap", true),
    DFYN("dfyn.svg", "dfyn", true),
    SUSHISWAP("sushiswap.png", "sushiswap", true),
    DMM("dmm.png", "dmm", true),
    BALANCER("balancer.png", "balancer", true),
    CONVEX("convex.png", "convex");

    val imageBasePath = "https://static.defitrack.io/images/protocols/"

    fun getImage(): String = imageBasePath + logo
}