package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.erc20.protocolspecific.*
import io.defitrack.logo.LogoService
import io.defitrack.market.pooling.contract.LPTokenContract
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class TokenService(
    private val erc20Service: ERC20Service,
    private val erC20Repository: ERC20Repository,
    private val LPtokenService: LPtokenService,
    private val hopTokenService: HopTokenService,
    private val curveTokenService: CurveTokenService,
    private val setProtocolTokenService: SetProtocolTokenService,
    private val balancerTokenService: BalancerTokenService,
    private val nativeTokenService: NativeTokenService,
    private val velodromeTokenService: VelodromeTokenService,
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

    suspend fun getType(lp: LPTokenContract): TokenType {
        val symbol = lp.symbol()
        return when {
            symbol == "SLP" -> {
                TokenType.SUSHISWAP
            }

            symbol == "UNI-V2" -> {
                TokenType.UNISWAP
            }

            symbol == "WLP" -> {
                TokenType.WAULT
            }

            symbol == "spLP" -> {
                TokenType.SPOOKY
            }

            symbol == "SPIRIT-LP" -> {
                TokenType.SPIRIT
            }

            symbol == "DFYNLP" -> {
                TokenType.DFYN
            }

            symbol == "APE-LP" -> {
                TokenType.APE
            }

            isBalancerLp(lp.address, lp.blockchainGateway.network) -> {
                TokenType.BALANCER
            }

            isSetLp(lp.address, lp.blockchainGateway.network) -> {
                TokenType.SET
            }

            isVelodromeLp(lp.address, lp.blockchainGateway.network) -> {
                TokenType.VELODROME
            }

            isHopLp(symbol) -> {
                TokenType.HOP
            }

            isCurveToken(lp.name()) -> {
                TokenType.CURVE
            }

            isKyberDMMLP(symbol) -> {
                TokenType.KYBER
            }

            else -> {
                TokenType.SINGLE
            }
        }
    }

    private fun isCurveToken(name: String): Boolean {
        return name.lowercase().startsWith("curve.fi".lowercase())
    }

    private suspend fun isBalancerLp(address: String, network: Network): Boolean {
        return balancerTokenService.isBalancerToken(address, network)
    }

    private suspend fun isSetLp(address: String, network: Network): Boolean {
        return setProtocolTokenService.isSetToken(address, network)
    }

    private suspend fun isVelodromeLp(address: String, network: Network): Boolean {
        return velodromeTokenService.isVelodromeToken(address, network)
    }

    val tokenInformationCache = Cache.Builder().build<String, TokenInformation>()

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenService.getNativeToken(network)
        }
        logger.info("getting token information for ${address} on ${network}")
        return tokenInformationCache.get("${address}-${network}") {
            val token = erc20Service.getERC20(network, address)
            when {
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
                    fromLP(Protocol.SUSHISWAP, network, token)
                }

                (token.symbol) == "UNI-V2" -> {
                    fromLP(if (network == Network.POLYGON) Protocol.QUICKSWAP else Protocol.UNISWAP, network, token)
                }

                (token.symbol) == "spLP" -> {
                    fromLP(Protocol.SPOOKY, network, token)
                }

                (token.symbol == "DFYNLP") -> {
                    fromLP(Protocol.DFYN, network, token)
                }

                (token.symbol == "APE-LP") -> {
                    fromLP(Protocol.APESWAP, network, token)
                }

                (token.symbol == "SPIRIT-LP") -> {
                    fromLP(Protocol.SPIRITSWAP, network, token)
                }

                isKyberDMMLP(token.symbol) -> {
                    fromLP(Protocol.KYBER_SWAP, network, token)
                }

                isBalancerLp(token.address, network) -> {
                    balancerTokenService.getTokenInformation(token.address, network)
                }

                isVelodromeLp(token.address, network) -> {
                    fromLP(Protocol.VELODROME, network, token)
                }

                isSetLp(token.address, network) -> {
                    setProtocolTokenService.getTokenInformation(token.address, network)
                }

                isHopLp(token.symbol) -> {
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

    private fun isHopLp(symbol: String): Boolean {
        return symbol.startsWith("HOP-LP")
    }

    suspend fun fromLP(protocol: Protocol, network: Network, erc20: ERC20): TokenInformation {
        val lp = LPtokenService.getLP(network, erc20.address)

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
            type = getType(lp),
            protocol = protocol,
            underlyingTokens = listOf(token0, token1),
            network = network
        )
    }
}