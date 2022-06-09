package io.defitrack.protocol

enum class Protocol(
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val enabled: Boolean = true
) {

    POLYGON(
        logo = "polygon.png",
        slug = "polygon_protocol",
        website = "https://polygon.technology",
        primitives = listOf(DefiPrimitive.FARMING),
    ),
    AAVE(
        logo = "aave.png",
        slug = "aave",
        website = "https://aave.com/",
        primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING)
    ),
    HUMANDAO(
        logo = "humandao.jpg",
        slug = "humandao",
        website = "https://humandao.org/",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    CURVE(
        logo = "curve.png",
        slug = "curve",
        website = "https://curve.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    MSTABLE(
        logo = "mstable.png",
        slug = "mstable",
        website = "https://mstable.org",
        primitives = listOf(DefiPrimitive.LENDING)
    ),
    COMPOUND(
        logo = "compound.png",
        slug = "compound",
        website = "https://compound.finance",
        primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING)
    ),
    BEEFY(
        logo = "beefy.png",
        slug = "beefy",
        website = "https://beefy.com",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    QUICKSWAP(
        logo = "quickswap.png",
        slug = "quickswap",
        website = "https://quickswap.exchange/",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    POLYCAT(
        logo = "polycat.webp",
        slug = "polycat",
        website = "https://polycat.finance",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    HOP(
        logo = "hop.jpg",
        slug = "hop",
        website = "https://hop.exchange",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    DINOSWAP(
        logo = "dinoswap.png",
        slug = "dinoswap",
        website = "https://dinoswap.exchange",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    ADAMANT(
        logo = "adamant.png",
        slug = "adamant",
        website = "https://adamant.finance",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    UNISWAP(
        logo = "uniswap.png",
        slug = "uniswap",
        website = "https://uniswap.org",
        primitives = listOf(element = DefiPrimitive.POOLING)
    ),
    DFYN(
        logo = "dfyn.svg",
        slug = "dfyn",
        website = "https://dfyn.network",
        primitives = listOf(DefiPrimitive.POOLING)
    ),
    IDEX(
        logo = "idex.png",
        slug = "idex",
        website = "https://idex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    SUSHISWAP(
        logo = "sushiswap.png",
        slug = "sushiswap",
        website = "https://sushi.com",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    DMM(
        logo = "dmm.png",
        slug = "dmm",
        website = "https://kyberswap.com",
        primitives = listOf(DefiPrimitive.POOLING)
    ),
    BALANCER(
        logo = "balancer.png",
        slug = "balancer",
        website = "https://balancer.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    STARGATE(
        logo = "stargate.png",
        slug = "stargate",
        website = "https://stargate.finance",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    SPOOKY(
        logo = "spooky.png",
        slug = "spooky",
        website = "https://spooky.fi",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    SPIRITSWAP(
        logo = "spirit.png",
        slug = "spirit",
        website = "https://www.spiritswap.finance",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    RIBBON(
        logo = "ribbon.png",
        slug = "ribbon",
        website = "https://ribbon.finance",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    LOOKSRARE(
        logo = "looksrare.png",
        slug = "looksrare",
        website = "https://looksrare.org",
        primitives = listOf(DefiPrimitive.FARMING)
    ),
    APESWAP(
        logo = "apeswap.png",
        slug = "apeswap",
        website = "https://apeswap.finance",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    DODO(
        logo = "dodo.png",
        slug = "dodo",
        website = "https://dodoex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING)
    ),
    CONVEX(
        logo = "convex.png",
        slug = "convex",
        website = "https://convexfinance.com/",
        primitives = listOf(DefiPrimitive.FARMING)
    );

    val imageBasePath = "https://github.com/defitrack/data/raw/master/logo/protocol/"

    fun getImage(): String = imageBasePath + logo
}