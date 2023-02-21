package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.erc20.protocolspecific.*
import io.defitrack.logo.LogoService
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class TokenService(
    private val erc20ContractReader: ERC20ContractReader,
    private val erC20Repository: ERC20Repository,
    private val LpContractReader: LpContractReader,
    private val hopTokenService: HopTokenService,
    private val curveTokenService: CurveTokenService,
    private val setProtocolTokenService: SetProtocolTokenService,
    private val balancerTokenService: BalancerTokenService,
    private val nativeTokenService: NativeTokenService,
    private val velodromeTokenService: VelodromeTokenService,
    private val kyberElasticTokenService: KyberElasticTokenService,
    private val logoService: LogoService
) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    val tokenCache: Cache<String, List<TokenInformation>> = Cache.Builder().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 3)
    fun refreshCaches() {
        refreshCache()
    }

    fun refreshCache() = runBlocking {
        logger.info("refreshing token cache")
        val semaphore = Semaphore(16)
        Network.values().map { network ->
            val millis = measureTimeMillis {
                val tokens = erC20Repository.allTokens(network).map {
                    async {
                        try {
                            semaphore.withPermit {
                                getTokenInformation(it, network)
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                tokenCache.put("tokens-${network}", tokens)
            }
            logger.info("refreshing token cache for ${network} took ${millis / 1000}s")
        }
    }


    suspend fun getAllTokensForNetwork(network: Network): List<TokenInformation> {
        return tokenCache.get("tokens-${network}") ?: emptyList()
    }

    private fun isCurveToken(name: String): Boolean {
        return name.lowercase().startsWith("curve.fi".lowercase())
    }

    private suspend fun isBalancerLp(token: ERC20): Boolean {
        return balancerTokenService.isProtocolToken(token)
    }

    private suspend fun isSetLp(token: ERC20): Boolean {
        return setProtocolTokenService.isProtocolToken(token)
    }

    private suspend fun isVelodromeLp(token: ERC20): Boolean {
        return velodromeTokenService.isVelodromeToken(token)
    }

    private suspend fun isKyberElasticLp(token: ERC20): Boolean {
        return kyberElasticTokenService.isProtocolToken(token)
    }

    val tokenInformationCache = Cache.Builder().build<String, TokenInformation>()

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenService.getNativeToken(network)
        }
        return tokenInformationCache.get("${address}-${network}") {
            val token = erc20ContractReader.getERC20(network, address)
            when {
                (network == Network.ETHEREUM && token.address.lowercase() == "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2") -> {
                    TokenInformation(
                        logo = logoService.generateLogoUrl(network, address),
                        name = "Maker",
                        symbol = "MKR",
                        address = token.address,
                        decimals = token.decimals,
                        totalSupply = token.totalSupply,
                        type = TokenType.SINGLE,
                        network = network
                    )
                }

                (token.name.startsWith("PoolTogether")) -> {
                    TokenInformation(
                        logo = logoService.generateLogoUrl(network, address),
                        name = token.name,
                        symbol = token.symbol,
                        address = token.address,
                        decimals = token.decimals,
                        totalSupply = token.totalSupply,
                        type = TokenType.POOLTOGETHER,
                        network = network
                    )
                }

                (token.symbol) == "SLP" -> {
                    fromLP(Protocol.SUSHISWAP, network, token, TokenType.SUSHISWAP)
                }

                (token.symbol) == "UNI-V2" -> {
                    fromLP(
                        if (network == Network.POLYGON) Protocol.QUICKSWAP else Protocol.UNISWAP, network, token,
                        if (network == Network.POLYGON) TokenType.QUICKSWAP else TokenType.UNISWAP
                    )
                }

                (token.symbol) == "spLP" -> {
                    fromLP(Protocol.SPOOKY, network, token, TokenType.SPOOKY)
                }

                (token.symbol == "DFYNLP") -> {
                    fromLP(Protocol.DFYN, network, token, TokenType.DFYN)
                }

                (token.symbol == "APE-LP") -> {
                    fromLP(Protocol.APESWAP, network, token, TokenType.APE)
                }

                (token.symbol == "SPIRIT-LP") -> {
                    fromLP(Protocol.SPIRITSWAP, network, token, TokenType.SPIRIT)
                }

                isKyberDMMLP(token.symbol) -> {
                    fromLP(Protocol.KYBER_SWAP, network, token, TokenType.KYBER)
                }

                isBalancerLp(token) -> {
                    balancerTokenService.getTokenInformation(token.address, network)
                }

                isVelodromeLp(token) -> {
                    fromLP(Protocol.VELODROME, network, token, TokenType.VELODROME)
                }

                isKyberElasticLp(token) -> {
                    fromLP(Protocol.KYBER_SWAP, network, token, TokenType.KYBER_ELASTIC)
                }

                isSetLp(token) -> {
                    setProtocolTokenService.getTokenInformation(token.address, network)
                }

                isHopLp(token) -> {
                    hopTokenService.getTokenInformation(token.address, network)
                }

                isCurveToken(token.name) -> {
                    curveTokenService.getTokenInformation(token.address, network);
                }

                else -> {
                    TokenInformation(
                        logo = logoService.generateLogoUrl(network, address),
                        name = token.name,
                        symbol = token.symbol,
                        address = token.address,
                        decimals = token.decimals,
                        totalSupply = token.totalSupply,
                        type = TokenType.SINGLE,
                        network = network
                    )
                }
            }
        }
    }

    private fun isKyberDMMLP(symbol: String): Boolean {
        return symbol.startsWith("DMM-LP")
    }

    private fun isHopLp(token: ERC20): Boolean {
        return hopTokenService.isProtocolToken(token)
    }

    suspend fun fromLP(protocol: Protocol, network: Network, erc20: ERC20, tokenType: TokenType): TokenInformation {
        val lp = LpContractReader.getLP(network, erc20.address)

        val token0 = getTokenInformation(
            lp.token0(), network
        )
        val token1 = getTokenInformation(
            lp.token1(), network
        )

        return TokenInformation(
            name = "${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply(),
            type = tokenType,
            protocol = protocol,
            underlyingTokens = listOf(token0, token1),
            network = network
        )
    }
}