package io.defitrack.protocol

import io.defitrack.common.network.Network

enum class Protocol(
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val enabled: Boolean = true,
    val networks: List<Network>
) {
    AURA(
        logo = "aura.png",
        slug = "aura",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://aura.finance",
        networks = listOf(Network.ETHEREUM)
    ),
    LIDO(
        logo = "lido.png",
        slug = "lido",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://lido.fi/",
        networks = listOf(Network.ETHEREUM)
    ),
    VELODROME(
        logo = "velodrome.svg",
        slug = "velodrome",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        "https://app.velodrome.finance",
        networks = listOf(Network.OPTIMISM)
    ),
    SWAPFISH(
        logo = "swapfish.png",
        slug = "swapfish",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://swapfish.fi",
        networks = listOf(Network.ARBITRUM)
    ),
    POOLTOGETHER(
        logo = "pooltogether.jpg",
        slug = "pooltogether",
        website = "https://pooltogether.com",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.ETHEREUM)
    ),
    QIDAO(
        logo = "qidao.png",
        slug = "qidao",
        website = "https://mai.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    TOKEMAK(
        logo = "tokemak.png",
        slug = "tokemak",
        website = "https://tokemak.xyz",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    CHAINLINK(
        logo = "chainlink.png",
        slug = "chainlink",
        website = "https://chain.link",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    OLYMPUSDAO(
        logo = "olympusdao.png",
        slug = "olympusdao",
        website = "https://www.olympusdao.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    BEETHOVENX(
        logo = "beethovenx.png",
        slug = "beethovenx",
        website = "https://beets.fi",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.POOLING),
        networks = listOf(Network.FANTOM)
    ),
    SET(
        logo = "set.png",
        slug = "set",
        website = "https://setprotocol.com",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.POLYGON, Network.POLYGON)
    ),
    WEPIGGY(
        logo = "wepiggy.png",
        slug = "wepiggy",
        website = "https://wepiggy.com",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.POLYGON)
    ),
    AELIN(
        logo = "aelin.jpeg",
        slug = "aelin",
        website = "https://aelin.xyz",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.OPTIMISM)
    ),
    BANCOR(
        logo = "bancor.png",
        slug = "bancor",
        website = "https://bancor.network",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    POLYGON(
        logo = "polygon.png",
        slug = "polygon-protocol",
        website = "https://polygon.technology",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    IRON_BANK(
        logo = "iron-bank.png",
        slug = "iron-bank",
        website = "https://ib.xyz/",
        primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING),
        networks = listOf(Network.ETHEREUM, Network.AVALANCHE, Network.FANTOM)
    ),
    AAVE(
        logo = "aave.png",
        slug = "aave",
        website = "https://aave.com/",
        primitives = listOf(
            DefiPrimitive.LENDING,
            DefiPrimitive.BORROWING,
            DefiPrimitive.FARMING,
            DefiPrimitive.POOLING
        ),
        networks = listOf(Network.ETHEREUM, Network.POLYGON, Network.ARBITRUM, Network.OPTIMISM)
    ),
    HUMANDAO(
        logo = "humandao.jpg",
        slug = "humandao",
        website = "https://humandao.org/",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    CURVE(
        logo = "curve.png",
        slug = "curve",
        website = "https://curve.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    MSTABLE(
        logo = "mstable.png",
        slug = "mstable",
        website = "https://mstable.org",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.POLYGON, Network.ETHEREUM)
    ),
    COMPOUND(
        logo = "compound.png",
        slug = "compound",
        website = "https://compound.finance",
        primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING),
        networks = listOf(Network.ETHEREUM)
    ),
    BEEFY(
        logo = "beefy.png",
        slug = "beefy",
        website = "https://beefy.com",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON, Network.AVALANCHE, Network.ARBITRUM, Network.FANTOM, Network.BINANCE)
    ),
    QUICKSWAP(
        logo = "quickswap.png",
        slug = "quickswap",
        website = "https://quickswap.exchange/",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    POLYCAT(
        logo = "polycat.webp",
        slug = "polycat",
        website = "https://polycat.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    HOP(
        logo = "hop.jpg",
        slug = "hop",
        website = "https://hop.exchange",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    DINOSWAP(
        logo = "dinoswap.png",
        slug = "dinoswap",
        website = "https://dinoswap.exchange",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    ADAMANT(
        logo = "adamant.png",
        slug = "adamant",
        website = "https://adamant.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    UNISWAP(
        logo = "uniswap.png",
        slug = "uniswap",
        website = "https://uniswap.org",
        primitives = listOf(element = DefiPrimitive.POOLING),
        networks = listOf(Network.ETHEREUM, Network.POLYGON)
    ),
    DFYN(
        logo = "dfyn.svg",
        slug = "dfyn",
        website = "https://dfyn.network",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.POLYGON)
    ),
    IDEX(
        logo = "idex.png",
        slug = "idex",
        website = "https://idex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    SUSHISWAP(
        logo = "sushiswap.png",
        slug = "sushiswap",
        website = "https://sushi.com",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON, Network.AVALANCHE, Network.ARBITRUM, Network.FANTOM, Network.ETHEREUM)
    ),
    KYBER_SWAP(
        logo = "dmm.png",
        slug = "kyberswap",
        website = "https://kyberswap.com",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.ETHEREUM, Network.POLYGON)
    ),
    BALANCER(
        logo = "balancer.png",
        slug = "balancer",
        website = "https://balancer.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON, Network.ARBITRUM)
    ),
    STARGATE(
        logo = "stargate.png",
        slug = "stargate",
        website = "https://stargate.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.OPTIMISM)
    ),
    SPOOKY(
        logo = "spooky.png",
        slug = "spooky",
        website = "https://spooky.fi",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.FANTOM)
    ),
    SPIRITSWAP(
        logo = "spirit.png",
        slug = "spirit",
        website = "https://www.spiritswap.finance",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.FANTOM)
    ),
    RIBBON(
        logo = "ribbon.png",
        slug = "ribbon",
        website = "https://ribbon.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.AVALANCHE, Network.ETHEREUM)
    ),
    LOOKSRARE(
        logo = "looksrare.png",
        slug = "looksrare",
        website = "https://looksrare.org",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = emptyList()
    ),
    APESWAP(
        logo = "apeswap.png",
        slug = "apeswap",
        website = "https://apeswap.finance",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON)
    ),
    DODO(
        logo = "dodo.png",
        slug = "dodo",
        website = "https://dodoex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON, Network.ARBITRUM, Network.BINANCE, Network.ETHEREUM)
    ),
    CONVEX(
        logo = "convex.png",
        slug = "convex",
        website = "https://convexfinance.com/",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM)
    ),
    MAKERDAO(
        logo = "makerdao.png",
        slug = "makerdao",
        website = "https://makerdao.com/",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.ETHEREUM)
    ),
    MAPLEFINANCE(
        logo = "maple-finance.png",
        slug = "maplefinance",
        website = "https://maple.finance/",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.ETHEREUM)
    );


    val imageBasePath = "https://github.com/defitrack/data/raw/master/logo/protocol/"

    fun getImage(): String = imageBasePath + logo
}