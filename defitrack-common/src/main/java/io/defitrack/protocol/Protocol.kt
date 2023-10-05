package io.defitrack.protocol

import io.defitrack.common.network.Network

enum class Protocol(
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val enabled: Boolean = true,
    val networks: List<Network>,
    val company: Company,
) {
    BASESWAP(
        logo = "baseswap.png",
        slug = "baseswap",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        website = "https://baseswap.fi",
        networks = listOf(Network.BASE),
        company = Company.BASESWAP
    ),
    RADIANT(
        logo = "radiant.png",
        slug = "radiant",
        primitives = listOf(DefiPrimitive.LENDING),
        website = "https://radiant.capital/",
        networks = listOf(Network.ARBITRUM),
        company = Company.RADIANT
    ),
    OVIX(
        logo = "ovix.png",
        slug = "ovix",
        primitives = listOf(DefiPrimitive.LENDING),
        website = "https://www.0vix.com",
        networks = listOf(Network.POLYGON_ZKEVM),
        company = Company.OVIX
    ),
    ARPA(
        logo = "arpa.png",
        slug = "arpa",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://www.arpanetwork.io/",
        networks = listOf(Network.BASE),
        company = Company.ARPA
    ),
    MOONWELL(
        logo = "moonwell.png",
        slug = "moonwell",
        primitives = listOf(DefiPrimitive.LENDING),
        website = "https://baseswap.fi",
        networks = listOf(Network.BASE),
        company = Company.MOONWELL
    ),
    AUTOEARN(
        logo = "autoearn.png",
        slug = "autoearn",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.BASE, Network.ARBITRUM),
        website = "https://www.autoearn.finance/",
        company = Company.AUTOEARN
    ),
    ALIENBASE(
        logo = "alienbase.png",
        slug = "alienbase",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        website = "https://alienbase.xyz",
        networks = listOf(Network.BASE),
        company = Company.ALIENBASE
    ),
    AERODROME(
        logo = "aerodrome.png",
        slug = "aerodrome",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        website = "https://aerodrome.finance",
        networks = listOf(Network.BASE),
        company = Company.AERODROME
    ),
    BLUR(
        logo = "blur.png",
        slug = "blur",
        primitives = listOf(DefiPrimitive.POOLING),
        website = "https://blur.io",
        networks = listOf(Network.ETHEREUM),
        company = Company.BLUR
    ),
    STAKEFISH(
        logo = "stakefish.png",
        slug = "stakefish",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        website = "https://stake.fish",
        networks = listOf(Network.ETHEREUM),
        company = Company.STAKEFISH
    ),
    TORNADO_CASH(
        logo = "tornado.png",
        slug = "tornadocash",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://tornadocash.eth.link",
        networks = listOf(Network.ETHEREUM),
        company = Company.TORNADO_CASH
    ),
    GMX(
        logo = "gmx.png",
        slug = "gmx",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        website = "https://tornadocash.eth.link",
        networks = listOf(Network.ETHEREUM, Network.ARBITRUM),
        company = Company.GMX
    ),
    GAINS_NETWORK(
        logo = "gain.png",
        slug = "gains",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        website = "https://gains.trade",
        networks = listOf(Network.ARBITRUM),
        company = Company.GAINS
    ),
    SWELL(
        logo = "swell.png",
        slug = "swell",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://swellnetwork.io",
        networks = listOf(Network.ETHEREUM),
        company = Company.SWELL
    ),
    CAMELOT(
        logo = "camelot.png",
        slug = "camelot",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        website = "https://camelot.exchange/",
        networks = listOf(Network.ARBITRUM),
        company = Company.CAMELOT
    ),
    STARGATE(
        logo = "stargate.svg",
        slug = "stargate",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        website = "https://stargate.finance",
        networks = listOf(
            Network.ETHEREUM,
            Network.OPTIMISM,
            Network.POLYGON,
            Network.ARBITRUM,
        ),
        company = Company.STARGATE
    ),
    SOLIDLIZARD(
        logo = "solidlizard.png",
        slug = "solidlizard",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        website = "https://solidlizard.finance",
        networks = listOf(Network.ARBITRUM),
        company = Company.SOLIDLIZARD
    ),
    COWSWAP(
        logo = "cowswap.png",
        slug = "cowswap",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://cow.fi",
        networks = listOf(Network.ETHEREUM),
        company = Company.COWSWAP
    ),
    AURA(
        logo = "aura.jpeg",
        slug = "aura",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://aura.finance",
        networks = listOf(Network.ETHEREUM),
        company = Company.AURA
    ),
    LIDO(
        logo = "lido.png",
        slug = "lido",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://lido.fi/",
        networks = listOf(Network.ETHEREUM),
        company = Company.LIDO
    ),
    VELODROME_V2(
        logo = "velodrome.svg",
        slug = "velodrome_v2",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        "https://app.velodrome.finance",
        networks = listOf(Network.OPTIMISM),
        company = Company.VELODROME
    ),
    VELODROME_V1(
        logo = "velodrome.svg",
        slug = "velodrome_v1",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        "https://app.velodrome.finance",
        networks = listOf(Network.OPTIMISM),
        company = Company.VELODROME
    ),
    SWAPFISH(
        logo = "swapfish.png",
        slug = "swapfish",
        primitives = listOf(DefiPrimitive.FARMING),
        website = "https://swapfish.fi",
        networks = listOf(Network.ARBITRUM),
        company = Company.SWAPFISH
    ),
    POOLTOGETHER(
        logo = "pooltogether.jpg",
        slug = "pooltogether",
        website = "https://pooltogether.com",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.ETHEREUM),
        company = Company.POOLTOGETHER
    ),
    QIDAO(
        logo = "qidao.png",
        slug = "qidao",
        website = "https://mai.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.QIDAO
    ),
    CHAINLINK(
        logo = "chainlink.png",
        slug = "chainlink",
        website = "https://chain.link",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM),
        company = Company.CHAINLINK
    ),
    OLYMPUSDAO(
        logo = "olympusdao.png",
        slug = "olympusdao",
        website = "https://www.olympusdao.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM),
        company = Company.OLYMPUSDAO
    ),
    BEETHOVENX(
        logo = "beethovenx.png",
        slug = "beethovenx",
        website = "https://beets.fi",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.POOLING),
        networks = listOf(Network.OPTIMISM),
        company = Company.BEETHOVENX
    ),
    SET(
        logo = "set.png",
        slug = "set",
        website = "https://setprotocol.com",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.POLYGON, Network.POLYGON),
        company = Company.SET
    ),
    WEPIGGY(
        logo = "wepiggy.png",
        slug = "wepiggy",
        website = "https://wepiggy.com",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.POLYGON),
        company = Company.WEPIGGY
    ),
    AELIN(
        logo = "aelin.jpeg",
        slug = "aelin",
        website = "https://aelin.xyz",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.OPTIMISM),
        company = Company.AELIN
    ),
    BANCOR(
        logo = "bancor.png",
        slug = "bancor",
        website = "https://bancor.network",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM),
        company = Company.BANCOR
    ),
    POLYGON(
        logo = "polygon.png",
        slug = "polygon-protocol",
        website = "https://polygon.technology",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM),
        company = Company.POLYGON
    ),
    IRON_BANK(
        logo = "iron-bank.png",
        slug = "iron-bank",
        website = "https://ib.xyz/",
        primitives = listOf(DefiPrimitive.LENDING, DefiPrimitive.BORROWING),
        networks = listOf(Network.ETHEREUM),
        company = Company.IRON_BANK
    ),
    AAVE_V2(
        logo = "aave.png",
        slug = "aave_v2",
        website = "https://aave.com/",
        primitives = listOf(
            DefiPrimitive.LENDING,
            DefiPrimitive.BORROWING,
            DefiPrimitive.FARMING,
            DefiPrimitive.POOLING
        ),
        company = Company.AAVE,
        networks = listOf(Network.ETHEREUM, Network.POLYGON, Network.ARBITRUM, Network.OPTIMISM)
    ),
    AAVE_V3(
        logo = "aave.png",
        slug = "aave_v3",
        website = "https://aave.com/",
        primitives = listOf(
            DefiPrimitive.LENDING,
            DefiPrimitive.BORROWING,
            DefiPrimitive.FARMING,
            DefiPrimitive.POOLING,
            DefiPrimitive.CLAIMABLES,
        ),
        company = Company.AAVE,
        networks = listOf(Network.ETHEREUM, Network.POLYGON, Network.ARBITRUM, Network.OPTIMISM)
    ),
    CURVE(
        logo = "curve.png",
        slug = "curve",
        website = "https://curve.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.ETHEREUM),
        company = Company.CURVE
    ),
    MSTABLE(
        logo = "mstable.png",
        slug = "mstable",
        website = "https://mstable.org",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.POLYGON, Network.ETHEREUM),
        company = Company.MSTABLE
    ),
    COMPOUND(
        logo = "compound.png",
        slug = "compound",
        website = "https://compound.finance",
        primitives = listOf(
            DefiPrimitive.LENDING,
            DefiPrimitive.BORROWING,
            DefiPrimitive.FARMING,
            DefiPrimitive.CLAIMABLES
        ),
        networks = listOf(Network.ETHEREUM),
        company = Company.COMPOUND
    ),
    BEEFY(
        logo = "beefy.png",
        slug = "beefy",
        website = "https://beefy.com",
        primitives = listOf(DefiPrimitive.FARMING),
        company = Company.BEEFY,
        networks = listOf(Network.POLYGON, Network.ARBITRUM)
    ),
    QUICKSWAP(
        logo = "quickswap.png",
        slug = "quickswap",
        website = "https://quickswap.exchange/",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.QUICKSWAP
    ),
    POLYCAT(
        logo = "polycat.webp",
        slug = "polycat",
        website = "https://polycat.finance",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.POLYCAT
    ),
    HOP(
        logo = "hop.jpg",
        slug = "hop",
        website = "https://hop.exchange",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.HOP
    ),
    DINOSWAP(
        logo = "dinoswap.png",
        slug = "dinoswap",
        website = "https://dinoswap.exchange",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.DINOSWAP
    ),
    ADAMANT(
        logo = "adamant.png",
        slug = "adamant",
        website = "https://adamant.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.ADAMANT
    ),
    UNISWAP_V2(
        logo = "uniswap.png",
        slug = "uniswap_v2",
        website = "https://uniswap.org",
        primitives = listOf(element = DefiPrimitive.POOLING),
        networks = listOf(Network.ETHEREUM, Network.POLYGON),
        company = Company.UNISWAP
    ),
    UNISWAP_V3(
        logo = "uniswap.png",
        slug = "uniswap_v3",
        website = "https://uniswap.org",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.ETHEREUM, Network.POLYGON),
        company = Company.UNISWAP
    ),
    DFYN(
        logo = "dfyn.svg",
        slug = "dfyn",
        website = "https://dfyn.network",
        primitives = listOf(DefiPrimitive.POOLING),
        networks = listOf(Network.POLYGON),
        company = Company.DFYN
    ),
    IDEX(
        logo = "idex.png",
        slug = "idex",
        website = "https://idex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.IDEX
    ),
    SUSHISWAP(
        logo = "sushiswap.png",
        slug = "sushiswap",
        website = "https://sushi.com",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        company = Company.SUSHISWAP,
        networks = listOf(Network.POLYGON, Network.ARBITRUM, Network.ETHEREUM)
    ),
    KYBER_SWAP(
        logo = "dmm.png",
        slug = "kyberswap",
        website = "https://kyberswap.com",
        primitives = listOf(DefiPrimitive.POOLING),
        company = Company.KYBER_SWAP,
        networks = listOf(Network.ETHEREUM, Network.POLYGON)
    ),
    BALANCER(
        logo = "balancer.png",
        slug = "balancer",
        website = "https://balancer.fi",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        company = Company.BALANCER,
        networks = listOf(Network.POLYGON, Network.ARBITRUM)
    ),
    RIBBON(
        logo = "ribbon.png",
        slug = "ribbon",
        website = "https://ribbon.finance",
        primitives = listOf(DefiPrimitive.FARMING),
        networks = listOf(Network.ETHEREUM),
        company = Company.RIBBON
    ),
    LOOKSRARE(
        logo = "looksrare.png",
        slug = "looksrare",
        website = "https://looksrare.org",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = emptyList(),
        company = Company.LOOKSRARE
    ),
    APESWAP(
        logo = "apeswap.png",
        slug = "apeswap",
        website = "https://apeswap.finance",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.APESWAP
    ),
    DODO(
        logo = "dodo.png",
        slug = "dodo",
        website = "https://dodoex.io",
        primitives = listOf(DefiPrimitive.POOLING, DefiPrimitive.FARMING),
        company = Company.DODO,
        networks = listOf(Network.POLYGON, Network.ARBITRUM, Network.ETHEREUM)
    ),
    CONVEX(
        logo = "convex.png",
        slug = "convex",
        website = "https://convexfinance.com/",
        primitives = listOf(DefiPrimitive.FARMING, DefiPrimitive.CLAIMABLES),
        networks = listOf(Network.ETHEREUM),
        company = Company.CONVEX
    ),
    MAKERDAO(
        logo = "makerdao.png",
        slug = "makerdao",
        website = "https://makerdao.com/",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.ETHEREUM),
        company = Company.MAKERDAO
    ),
    MAPLEFINANCE(
        logo = "maple-finance.png",
        slug = "maplefinance",
        website = "https://maple.finance/",
        primitives = listOf(DefiPrimitive.LENDING),
        networks = listOf(Network.ETHEREUM),
        company = Company.MAPLEFINANCE
    );


    val imageBasePath = "https://github.com/defitrack/data/raw/master/logo/protocol/"

    fun getImage(): String = imageBasePath + logo
}