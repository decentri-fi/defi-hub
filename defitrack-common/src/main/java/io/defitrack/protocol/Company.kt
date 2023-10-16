package io.defitrack.protocol

enum class Company(
    val prettyName: String,
    val slug: String
) {
    ALIENBASE(
        prettyName = "Alienbase",
        slug = "alienbase"
    ),
    SONNE(
        prettyName = "Sonne",
        slug = "sonne"
    ),
    PIKA(
        prettyName = "Pika",
        slug = "pika"
    ),
    ARPA(
        prettyName = "Arpa",
        slug = "arpa"
    ),
    THALES(
        prettyName = "Thales",
        slug = "thales"
    ),
    TRADER_JOE(
        prettyName = "TraderJoe",
        slug = "trader-joe"
    ),
    MOONWELL(
        prettyName = "Moonwell",
        slug = "moonwell"
    ),
    AUTOEARN(
        prettyName = "AutoEarn",
        slug = "autoearn"
    ),
    BASESWAP(
        prettyName = "Baseswap",
        slug = "baseswap"
    ),
    OVIX(
        prettyName = "0VIX",
        slug = "ovix"
    ),
    AERODROME(
        prettyName = "Aerodrome",
        slug = "aerodrome"
    ),
    BLUR(
        prettyName = "Blur",
        slug = "blur"
    ),
    TORNADO_CASH(
        prettyName = "Tornado Cash",
        slug = "tornadocash"
    ),
    STAKEFISH(
        prettyName = "Stakefish",
        slug = "stakefish"
    ),
    GMX(
        prettyName = "GMX",
        slug = "gmx"
    ),
    GAINS(
        prettyName = "Gains",
        slug = "gains"
    ),
    SWELL(
        prettyName = "Swell",
        slug = "swell"
    ),
    CAMELOT(
        prettyName = "Camelot",
        slug = "camelot"
    ),
    STARGATE(
        prettyName = "Stargate",
        slug = "stargate"
    ),
    SOLIDLIZARD(
        prettyName = "SolidLizard",
        slug = "solidlizard"
    ),
    COWSWAP(
        prettyName = "CowSwap",
        slug = "cowswap"
    ),
    AURA(
        prettyName = "Aura Finance",
        slug = "aura"
    ),
    LIDO(
        prettyName = "Lido",
        slug = "lido"
    ),
    VELODROME(
        prettyName = "Velodrome",
        slug = "velodrome"
    ),
    SWAPFISH(
        prettyName = "SwapFish",
        slug = "swapfish"
    ),
    POOLTOGETHER(
        prettyName = "PoolTogether",
        slug = "pooltogether"
    ),
    QIDAO(
        prettyName = "QiDao",
        slug = "qidao"
    ),
    TOKEMAK(
        prettyName = "Tokemak",
        slug = "tokemak"
    ),
    CHAINLINK(
        prettyName = "Chainlink",
        slug = "chainlink"
    ),
    OLYMPUSDAO(
        prettyName = "OlympusDAO",
        slug = "olympusdao"
    ),
    BEETHOVENX(
        prettyName = "BeethovenX",
        slug = "beethovenx"
    ),
    SET(
        prettyName = "Set Protocol",
        slug = "set"
    ),
    WEPIGGY(
        prettyName = "WePiggy",
        slug = "wepiggy"
    ),
    AELIN(
        prettyName = "Aelin",
        slug = "aelin"
    ),
    BANCOR(
        prettyName = "Bancor",
        slug = "bancor"
    ),
    POLYGON(
        prettyName = "Polygon",
        slug = "polygon-protocol"
    ),
    IRON_BANK(
        prettyName = "Iron Bank",
        slug = "iron-bank"
    ),
    AAVE(
        prettyName = "Aave",
        slug = "aave"
    ),
    RADIANT(
        prettyName = "Radiant",
        slug = "radiant"
    ),
    CURVE(
        prettyName = "Curve",
        slug = "curve"
    ),
    MSTABLE(
        prettyName = "mStable",
        slug = "mstable"
    ),
    COMPOUND(
        prettyName = "Compound",
        slug = "compound"
    ),
    BEEFY(
        prettyName = "Beefy",
        slug = "beefy"
    ),
    QUICKSWAP(
        prettyName = "QuickSwap",
        slug = "quickswap"
    ),
    POLYCAT(
        prettyName = "Polycat",
        slug = "polycat"
    ),
    HOP(
        prettyName = "Hop Protocol",
        slug = "hop"
    ),
    DINOSWAP(
        prettyName = "DinoSwap",
        slug = "dinoswap"
    ),
    ADAMANT(
        prettyName = "Adamant",
        slug = "adamant"
    ),
    UNISWAP(
        prettyName = "Uniswap",
        slug = "uniswap"
    ),
    DFYN(
        prettyName = "DFYN",
        slug = "dfyn"
    ),
    IDEX(
        prettyName = "IDEX",
        slug = "idex"
    ),
    SUSHISWAP(
        prettyName = "SushiSwap",
        slug = "sushiswap"
    ),
    KYBER_SWAP(
        prettyName = "Kyber Swap",
        slug = "kyberswap"
    ),
    BALANCER(
        prettyName = "Balancer",
        slug = "balancer"
    ),
    RIBBON(
        prettyName = "Ribbon",
        slug = "ribbon"
    ),
    LOOKSRARE(
        prettyName = "LooksRare",
        slug = "looksrare"
    ),
    APESWAP(
        prettyName = "ApeSwap",
        slug = "apeswap"
    ),
    DODO(
        prettyName = "DODO",
        slug = "dodo"
    ),
    CONVEX(
        prettyName = "Convex",
        slug = "convex"
    ),
    MAKERDAO(
        prettyName = "MakerDAO",
        slug = "makerdao"
    ),
    MAPLEFINANCE(
        prettyName = "Maple Finance",
        slug = "maplefinance"
    ),
    KWENTA(
        prettyName = "Kwenta",
        slug = "kwenta"
    ),
    EXTRA_FINANCE(
        prettyName = "ExtraFinance",
        slug = "extra-finance"
    ),
    METAVAULT(
        prettyName = "MetaVault",
        slug = "metavault"
    );


    companion object {
        fun findByName(name: String) {
            entries.find {
                it.name.lowercase() == name.lowercase() || it.slug.lowercase() == name.lowercase()
            }
        }
    }

    fun fetchProtocols(): List<Protocol> {
        return Protocol.findByCompany(this)
    }
}