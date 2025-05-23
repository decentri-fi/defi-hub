package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.common.network.Network.*
import io.defitrack.protocol.DefiPrimitive.*

enum class Protocol(
    val logo: String,
    val slug: String,
    val primitives: List<DefiPrimitive>,
    val website: String,
    val enabled: Boolean = true,
    val networks: List<Network>,
    val company: Company,
) {
    PUFFER(
        logo = "puffer.png",
        slug = "puffer",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://puffer.fi",
        networks = listOf(ETHEREUM),
        company = Company.PUFFER
    ),
    ETHER_FI(
        logo = "etherfi.png",
        slug = "ether-fi",
        primitives = listOf(FARMING),
        website = "https://ether.fi",
        networks = listOf(ETHEREUM),
        company = Company.ETHER_FI
    ),
    PENDLE(
        logo = "pendle.png",
        slug = "pendle",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://pendle.finance",
        networks = listOf(ETHEREUM, ARBITRUM),
        company = Company.PENDLE
    ),
    STARKNET(
        logo = "starknet.png",
        slug = "starknet",
        primitives = listOf(CLAIMABLES),
        website = "https://www.starknet.io",
        networks = listOf(ETHEREUM),
        company = Company.STARKNET
    ),
    MAGPIE_XYZ(
        logo = "magpie.png",
        slug = "magpie-xyz",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://magpie.xyz",
        networks = listOf(ARBITRUM),
        company = Company.MAGPIE_XYZ
    ),
    STADER(
        logo = "stader.png",
        slug = "stader",
        primitives = listOf(FARMING),
        website = "https://www.staderlabs.com/",
        networks = listOf(Network.POLYGON),
        company = Company.STADER
    ),
    TANGIBLE(
        logo = "tangible.png",
        slug = "tangible",
        primitives = listOf(FARMING),
        website = "https://www.tangible.store/",
        networks = listOf(Network.POLYGON),
        company = Company.TANGIBLE
    ),
    MORPHO_AAVE_V3(
        logo = "morpho.png",
        slug = "morpho-aave-v3",
        primitives = listOf(LENDING),
        website = "https://morpho.finance",
        networks = listOf(ETHEREUM),
        company = Company.MORPHO
    ),
    EQUATION(
        logo = "equation.png",
        slug = "equation",
        primitives = listOf(FARMING),
        website = "https://equation.org",
        networks = listOf(ARBITRUM),
        company = Company.EQUATION
    ),
    EIGENLAYER(
        logo = "eigenlayer.png",
        slug = "eigenlayer",
        primitives = listOf(FARMING),
        website = "https://eigenlayer.xyz",
        networks = listOf(ETHEREUM),
        company = Company.EIGENLAYER
    ),
    MANTLE(
        logo = "mantle.png",
        slug = "mantle",
        primitives = listOf(FARMING),
        website = "https://mantle.xyz",
        networks = listOf(ETHEREUM),
        company = Company.MANTLE
    ),
    SEAMLESS(
        logo = "seamless.png",
        slug = "seamless",
        primitives = listOf(LENDING),
        website = "https://seamlessprotocol.com",
        networks = listOf(BASE),
        company = Company.SEAMLESS
    ),
    LYRA(
        logo = "lyra.png",
        slug = "lyra",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://lyra.finance",
        networks = listOf(ETHEREUM, OPTIMISM, ARBITRUM),
        company = Company.LYRA
    ),
    VESTA(
        logo = "vesta.png",
        slug = "vesta",
        primitives = listOf(POOLING, FARMING, CLAIMABLES, LENDING),
        website = "https://vestafinance.xyz",
        networks = listOf(ARBITRUM),
        company = Company.VESTA
    ),
    BLAST(
        logo = "blast.png",
        slug = "blast",
        primitives = listOf(POOLING),
        website = "https://blast.io",
        networks = listOf(ETHEREUM),
        company = Company.BLAST
    ),
    ONDO(
        logo = "ondo.png",
        slug = "ondo",
        primitives = listOf(POOLING, FARMING),
        website = "https://ondo.finance",
        networks = listOf(ETHEREUM),
        company = Company.PRISMA
    ),
    PRISMA(
        logo = "prisma.png",
        slug = "prisma",
        primitives = listOf(FARMING, LENDING, CLAIMABLES),
        website = "https://prismafinance.com/",
        networks = listOf(ETHEREUM),
        company = Company.PRISMA
    ),
    ROCKETPOOL(
        logo = "rocketpool.png",
        slug = "rocketpool",
        primitives = listOf(FARMING),
        website = "https://rocketpool.net",
        networks = listOf(ETHEREUM),
        company = Company.ROCKETPOOL
    ),
    SYNTHETIX(
        logo = "synthetix.png",
        slug = "synthetix",
        primitives = listOf(LENDING, FARMING, CLAIMABLES),
        website = "https://synthetix.io",
        networks = listOf(ETHEREUM),
        company = Company.SYNTHETIX
    ),
    FRAX(
        logo = "frax.png",
        slug = "frax",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://frax.finance",
        networks = listOf(ETHEREUM),
        company = Company.FRAX
    ),
    KLIMA_DAO(
        logo = "klimadao.png",
        slug = "klima-dao",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://www.klimadao.finance",
        networks = listOf(Network.POLYGON),
        company = Company.KLIMA_DAO
    ),
    PLUTUSDAO(
        logo = "plutusdao.png",
        slug = "plutusdao",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://plutusdao.io",
        networks = listOf(Network.POLYGON),
        company = Company.PLUTUSDAO
    ),
    PANCAKESWAP(
        logo = "pancakeswap.png",
        slug = "pancakeswap",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://pancakeswap.finance",
        networks = listOf(POLYGON_ZKEVM),
        company = Company.PANCAKESWAP
    ),
    BASESWAP(
        logo = "baseswap.png",
        slug = "baseswap",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://baseswap.fi",
        networks = listOf(BASE),
        company = Company.BASESWAP
    ),
    SPARK(
        logo = "spark.png",
        slug = "spark",
        primitives = listOf(LENDING),
        website = "https://sparkprotocol.io",
        networks = listOf(ETHEREUM),
        company = Company.SPARK
    ),
    EQUALIZER(
        logo = "equalizer.png",
        slug = "equalizer",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://equalizer.exchange",
        networks = listOf(BASE),
        company = Company.EQUALIZER
    ),
    SONNE(
        logo = "sonne.png",
        slug = "sonne",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://sonne.finance",
        networks = listOf(OPTIMISM),
        company = Company.SONNE
    ),
    PIKA(
        logo = "pika.png",
        slug = "pika",
        primitives = listOf(CLAIMABLES, FARMING),
        website = "https://www.pikaprotocol.com/",
        networks = listOf(OPTIMISM),
        company = Company.PIKA
    ),
    THALES(
        logo = "thales.png",
        slug = "thales",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://thalesmarket.io/",
        networks = listOf(OPTIMISM, BASE, ARBITRUM),
        company = Company.THALES
    ),
    TRADER_JOE(
        logo = "traderjoe.png",
        slug = "trader-joe",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://traderjoexyz.com",
        networks = listOf(ARBITRUM),
        company = Company.TRADER_JOE
    ),
    RADIANT(
        logo = "radiant.png",
        slug = "radiant",
        primitives = listOf(LENDING, FARMING, CLAIMABLES),
        website = "https://radiant.capital/",
        networks = listOf(ARBITRUM),
        company = Company.RADIANT
    ),
    OVIX(
        logo = "ovix.png",
        slug = "ovix",
        primitives = listOf(LENDING),
        website = "https://www.0vix.com",
        networks = listOf(POLYGON_ZKEVM),
        company = Company.OVIX
    ),
    ARPA(
        logo = "arpa.png",
        slug = "arpa",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://www.arpanetwork.io/",
        networks = listOf(BASE),
        company = Company.ARPA
    ),
    MOONWELL(
        logo = "moonwell.png",
        slug = "moonwell",
        primitives = listOf(LENDING, CLAIMABLES),
        website = "https://baseswap.fi",
        networks = listOf(BASE),
        company = Company.MOONWELL
    ),
    AUTOEARN(
        logo = "autoearn.png",
        slug = "autoearn",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(BASE, ARBITRUM),
        website = "https://www.autoearn.finance/",
        company = Company.AUTOEARN
    ),
    ALIENBASE(
        logo = "alienbase.png",
        slug = "alienbase",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://alienbase.xyz",
        networks = listOf(BASE),
        company = Company.ALIENBASE
    ),
    AERODROME(
        logo = "aerodrome.png",
        slug = "aerodrome",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://aerodrome.finance",
        networks = listOf(BASE),
        company = Company.AERODROME
    ),
    BLUR(
        logo = "blur.png",
        slug = "blur",
        primitives = listOf(POOLING, FARMING),
        website = "https://blur.io",
        networks = listOf(ETHEREUM),
        company = Company.BLUR
    ),
    STAKEFISH(
        logo = "stakefish.png",
        slug = "stakefish",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://stake.fish",
        networks = listOf(ETHEREUM),
        company = Company.STAKEFISH
    ),
    TORNADO_CASH(
        logo = "tornado.png",
        slug = "tornadocash",
        primitives = listOf(FARMING),
        website = "https://tornadocash.eth.link",
        networks = listOf(ETHEREUM),
        company = Company.TORNADO_CASH
    ),
    GMX(
        logo = "gmx.png",
        slug = "gmx",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://gmx.io",
        networks = listOf(ETHEREUM, ARBITRUM),
        company = Company.GMX
    ),
    GAINS_NETWORK(
        logo = "gain.png",
        slug = "gains",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://gains.trade",
        networks = listOf(ARBITRUM),
        company = Company.GAINS
    ),
    SWELL(
        logo = "swell.png",
        slug = "swell",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://swellnetwork.io",
        networks = listOf(ETHEREUM),
        company = Company.SWELL
    ),
    LIQUITY(
        logo = "liquity.png",
        slug = "liquity",
        primitives = listOf(FARMING, LENDING, CLAIMABLES),
        website = "https://liquity.org",
        networks = listOf(ETHEREUM),
        company = Company.LIQUITY
    ),
    MYCELIUM(
        logo = "mycelium.png",
        slug = "mycelium",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://mycelium.org",
        networks = listOf(ARBITRUM),
        company = Company.MYCELIUM
    ),
    CAMELOT(
        logo = "camelot.png",
        slug = "camelot",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://camelot.exchange/",
        networks = listOf(ARBITRUM),
        company = Company.CAMELOT
    ),
    STARGATE(
        logo = "stargate.svg",
        slug = "stargate",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://stargate.finance",
        networks = listOf(
            ETHEREUM,
            OPTIMISM,
            Network.POLYGON,
            ARBITRUM,
        ),
        company = Company.STARGATE
    ),
    SOLIDLIZARD(
        logo = "solidlizard.png",
        slug = "solidlizard",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        website = "https://solidlizard.finance",
        networks = listOf(ARBITRUM),
        company = Company.SOLIDLIZARD
    ),
    COWSWAP(
        logo = "cowswap.png",
        slug = "cowswap",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://cow.fi",
        networks = listOf(ETHEREUM),
        company = Company.COWSWAP
    ),
    AURA(
        logo = "aura.jpeg",
        slug = "aura",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://aura.finance",
        networks = listOf(ETHEREUM),
        company = Company.AURA
    ),
    LIDO(
        logo = "lido.png",
        slug = "lido",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://lido.fi/",
        networks = listOf(ETHEREUM),
        company = Company.LIDO
    ),
    VELODROME_V2(
        logo = "velodrome.svg",
        slug = "velodrome_v2",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        "https://app.velodrome.finance",
        networks = listOf(OPTIMISM),
        company = Company.VELODROME
    ),
    VELODROME_V1(
        logo = "velodrome.svg",
        slug = "velodrome_v1",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        "https://app.velodrome.finance",
        networks = listOf(OPTIMISM),
        company = Company.VELODROME
    ),
    SWAPFISH(
        logo = "swapfish.png",
        slug = "swapfish",
        primitives = listOf(FARMING, CLAIMABLES),
        website = "https://swapfish.fi",
        networks = listOf(ARBITRUM),
        company = Company.SWAPFISH
    ),
    POOLTOGETHER(
        logo = "pooltogether.jpg",
        slug = "pooltogether",
        website = "https://pooltogether.com",
        primitives = listOf(POOLING),
        networks = listOf(ETHEREUM),
        company = Company.POOLTOGETHER
    ),
    QIDAO(
        logo = "qidao.png",
        slug = "qidao",
        website = "https://mai.finance",
        primitives = listOf(FARMING, CLAIMABLES, LENDING),
        networks = listOf(Network.POLYGON),
        company = Company.QIDAO
    ),
    CHAINLINK(
        logo = "chainlink.png",
        slug = "chainlink",
        website = "https://chain.link",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(ETHEREUM),
        company = Company.CHAINLINK
    ),
    OLYMPUSDAO(
        logo = "olympusdao.png",
        slug = "olympusdao",
        website = "https://www.olympusdao.finance",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(ETHEREUM),
        company = Company.OLYMPUSDAO
    ),
    BEETHOVENX(
        logo = "beethovenx.png",
        slug = "beethovenx",
        website = "https://beets.fi",
        primitives = listOf(FARMING, POOLING, CLAIMABLES),
        networks = listOf(OPTIMISM),
        company = Company.BEETHOVENX
    ),
    SET(
        logo = "set.png",
        slug = "set",
        website = "https://setprotocol.com",
        primitives = listOf(POOLING),
        networks = listOf(Network.POLYGON, Network.POLYGON),
        company = Company.SET
    ),
    WEPIGGY(
        logo = "wepiggy.png",
        slug = "wepiggy",
        website = "https://wepiggy.com",
        primitives = listOf(LENDING),
        networks = listOf(Network.POLYGON),
        company = Company.WEPIGGY
    ),
    AELIN(
        logo = "aelin.jpeg",
        slug = "aelin",
        website = "https://aelin.xyz",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(OPTIMISM),
        company = Company.AELIN
    ),
    BANCOR(
        logo = "bancor.png",
        slug = "bancor",
        website = "https://bancor.network",
        primitives = listOf(POOLING, FARMING),
        networks = listOf(ETHEREUM),
        company = Company.BANCOR
    ),
    POLYGON(
        logo = "polygon.png",
        slug = "polygon-protocol",
        website = "https://polygon.technology",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(ETHEREUM),
        company = Company.POLYGON
    ),
    IRON_BANK(
        logo = "iron-bank.png",
        slug = "iron-bank",
        website = "https://ib.xyz/",
        primitives = listOf(LENDING, BORROWING),
        networks = listOf(ETHEREUM),
        company = Company.IRON_BANK
    ),
    AAVE_V2(
        logo = "aave.png",
        slug = "aave_v2",
        website = "https://aave.com/",
        primitives = listOf(
            LENDING,
            BORROWING,
            FARMING,
            POOLING,
            CLAIMABLES
        ),
        company = Company.AAVE,
        networks = listOf(ETHEREUM, Network.POLYGON, ARBITRUM, OPTIMISM)
    ),
    AAVE_V3(
        logo = "aave.png",
        slug = "aave_v3",
        website = "https://aave.com/",
        primitives = listOf(
            LENDING,
            BORROWING,
            FARMING,
            POOLING,
            CLAIMABLES,
        ),
        company = Company.AAVE,
        networks = listOf(ETHEREUM, Network.POLYGON, ARBITRUM, OPTIMISM)
    ),
    CURVE(
        logo = "curve.png",
        slug = "curve",
        website = "https://curve.fi",
        primitives = listOf(POOLING, FARMING, CLAIMABLES, LENDING),
        networks = listOf(ETHEREUM),
        company = Company.CURVE
    ),
    MSTABLE(
        logo = "mstable.png",
        slug = "mstable",
        website = "https://mstable.org",
        primitives = listOf(LENDING, FARMING, CLAIMABLES),
        networks = listOf(Network.POLYGON, ETHEREUM),
        company = Company.MSTABLE
    ),
    COMPOUND(
        logo = "compound.png",
        slug = "compound",
        website = "https://compound.finance",
        primitives = listOf(
            LENDING,
            BORROWING,
            FARMING,
            CLAIMABLES
        ),
        networks = listOf(ETHEREUM, ARBITRUM, BASE, Network.POLYGON),
        company = Company.COMPOUND
    ),
    BEEFY(
        logo = "beefy.png",
        slug = "beefy",
        website = "https://beefy.com",
        primitives = listOf(FARMING, CLAIMABLES),
        company = Company.BEEFY,
        networks = listOf(Network.POLYGON, ARBITRUM)
    ),
    QUICKSWAP(
        logo = "quickswap.png",
        slug = "quickswap",
        website = "https://quickswap.exchange/",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        networks = listOf(Network.POLYGON, POLYGON_ZKEVM),
        company = Company.QUICKSWAP
    ),
    POLYCAT(
        logo = "polycat.webp",
        slug = "polycat",
        website = "https://polycat.finance",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.POLYCAT
    ),
    HOP(
        logo = "hop.jpg",
        slug = "hop",
        website = "https://hop.exchange",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.HOP
    ),
    DINOSWAP(
        logo = "dinoswap.png",
        slug = "dinoswap",
        website = "https://dinoswap.exchange",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(Network.POLYGON),
        company = Company.DINOSWAP
    ),
    UNISWAP_V2(
        logo = "uniswap.png",
        slug = "uniswap_v2",
        website = "https://uniswap.org",
        primitives = listOf(element = POOLING),
        networks = listOf(ETHEREUM, Network.POLYGON),
        company = Company.UNISWAP
    ),
    UNISWAP_V3(
        logo = "uniswap.png",
        slug = "uniswap_v3",
        website = "https://uniswap.org",
        primitives = listOf(POOLING, CLAIMABLES),
        networks = listOf(ETHEREUM, Network.POLYGON),
        company = Company.UNISWAP
    ),
    DFYN(
        logo = "dfyn.svg",
        slug = "dfyn",
        website = "https://dfyn.network",
        primitives = listOf(POOLING),
        networks = listOf(Network.POLYGON),
        company = Company.DFYN
    ),
    IDEX(
        logo = "idex.png",
        slug = "idex",
        website = "https://idex.io",
        primitives = listOf(POOLING, FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.IDEX
    ),
    SUSHISWAP(
        logo = "sushiswap.png",
        slug = "sushiswap",
        website = "https://sushi.com",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        company = Company.SUSHISWAP,
        networks = listOf(Network.POLYGON, ARBITRUM, ETHEREUM)
    ),
    KYBER_SWAP(
        logo = "dmm.png",
        slug = "kyberswap",
        website = "https://kyberswap.com",
        primitives = listOf(POOLING),
        company = Company.KYBER_SWAP,
        networks = listOf(ETHEREUM, Network.POLYGON)
    ),
    BALANCER(
        logo = "balancer.png",
        slug = "balancer",
        website = "https://balancer.fi",
        primitives = listOf(POOLING, FARMING, CLAIMABLES),
        company = Company.BALANCER,
        networks = listOf(Network.POLYGON, ARBITRUM)
    ),
    RIBBON(
        logo = "ribbon.png",
        slug = "ribbon",
        website = "https://ribbon.finance",
        primitives = listOf(FARMING),
        networks = listOf(ETHEREUM),
        company = Company.RIBBON
    ),
    LOOKSRARE(
        logo = "looksrare.png",
        slug = "looksrare",
        website = "https://looksrare.org",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = emptyList(),
        company = Company.LOOKSRARE
    ),
    APESWAP(
        logo = "apeswap.png",
        slug = "apeswap",
        website = "https://apeswap.finance",
        primitives = listOf(POOLING, FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.APESWAP
    ),
    DODO(
        logo = "dodo.png",
        slug = "dodo",
        website = "https://dodoex.io",
        primitives = listOf(POOLING, FARMING),
        company = Company.DODO,
        networks = listOf(Network.POLYGON, ARBITRUM, ETHEREUM)
    ),
    CONVEX(
        logo = "convex.png",
        slug = "convex",
        website = "https://convexfinance.com/",
        primitives = listOf(FARMING, CLAIMABLES),
        networks = listOf(ETHEREUM),
        company = Company.CONVEX
    ),
    MAKERDAO(
        logo = "makerdao.png",
        slug = "makerdao",
        website = "https://makerdao.com/",
        primitives = listOf(LENDING),
        networks = listOf(ETHEREUM),
        company = Company.MAKERDAO
    ),
    KWENTA(
        logo = "kwenta.png",
        slug = "kwenta",
        website = "https://kwenta.io",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(OPTIMISM),
        company = Company.KWENTA
    ),
    EXTRA_FINANCE(
        logo = "extra-finance.png",
        slug = "extra-finance",
        website = "https://app.extrafi.io/",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(OPTIMISM),
        company = Company.EXTRA_FINANCE
    ),
    METAVAULT(
        logo = "metavault.png",
        slug = "metavault",
        website = "https://metavault.trade",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.METAVAULT
    ),
    SANDBOX(
        logo = "sandbox.png",
        slug = "sandbox",
        website = "https://sandbox.game",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(Network.POLYGON),
        company = Company.SANDBOX
    ),
    ETHOS(
        logo = "ethos.png",
        slug = "ethos",
        website = "https://ethos.finance",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(OPTIMISM),
        company = Company.ETHOS
    ),
    MUX(
        logo = "mux.png",
        slug = "mux",
        website = "https://mux.network",
        primitives = listOf(CLAIMABLES, FARMING),
        networks = listOf(ARBITRUM),
        company = Company.MUX
    );


    val imageBasePath = "https://github.com/decentri-fi/data/raw/master/logo/protocol/"

    fun getImage(): String = imageBasePath + logo

    companion object {
        fun findByCompany(company: Company): List<Protocol> {
            return entries.filter {
                it.company == company
            }
        }

        fun fromString(name: String): Protocol? {
            return Protocol.entries.find {
                it.slug == name || it.name == name
            }
        }
    }
}