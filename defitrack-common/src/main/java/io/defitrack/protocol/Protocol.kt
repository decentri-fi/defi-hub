package io.defitrack.protocol

enum class Protocol(
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val enabled: Boolean = true
) {

    AAVE("aave.png", "aave", primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING)),
    HUMANDAO("humandao.jpg", "humandao", primitives = listOf(DefiPrimitive.FARMING)),
    CURVE("curve.png", "curve", primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    MSTABLE("mstable.png", "mstable", primitives = listOf(DefiPrimitive.LENDING)),
    COMPOUND("compound.png", "compound", primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING)),
    BEEFY("beefy.png", "beefy", primitives = listOf(DefiPrimitive.FARMING)),
    QUICKSWAP("quickswap.png", "quickswap", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    POLYCAT("polycat.webp", "polycat", listOf(DefiPrimitive.FARMING)),
    HOP("hop.jpg", "hop", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    DINOSWAP("dinoswap.png", "dinoswap", listOf(DefiPrimitive.FARMING)),
    ADAMANT("adamant.png", "adamant", listOf(DefiPrimitive.FARMING)),
    UNISWAP("uniswap.png", "uniswap", listOf(DefiPrimitive.POOLING)),
    DFYN("dfyn.svg", "dfyn", listOf(DefiPrimitive.POOLING)),
    IDEX("idex.png", "idex", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    SUSHISWAP("sushiswap.png", "sushiswap", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    DMM("dmm.png", "dmm", listOf(DefiPrimitive.POOLING)),
    BALANCER("balancer.png", "balancer", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    STARGATE("stargate.png", "stargate", listOf(DefiPrimitive.FARMING)),
    SPOOKY("spooky.png", "spooky", listOf(DefiPrimitive.FARMING)),
    SPIRITSWAP("spirit.png", "spirit", listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)),
    CONVEX("convex.png", "convex", listOf(DefiPrimitive.FARMING));

    val imageBasePath = "https://static.defitrack.io/images/protocols/"

    fun getImage(): String = imageBasePath + logo
}