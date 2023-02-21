package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.erc20.protocolspecific.BalancerTokenService
import io.defitrack.erc20.protocolspecific.CurveTokenService
import io.defitrack.erc20.protocolspecific.HopTokenService
import io.defitrack.logo.LogoService
import io.defitrack.market.pooling.contract.LPTokenContract
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val erc20Service: ERC20Service,
    private val erC20Repository: ERC20Repository,
    private val LPtokenService: LPtokenService,
    private val hopTokenService: HopTokenService,
    private val curveTokenService: CurveTokenService,
    private val balancerTokenService: BalancerTokenService,
    private val nativeTokenService: NativeTokenService,
    private val logoService: LogoService
) {


    val tokenCache: Cache<String, List<TokenInformation>> = Cache.Builder().build()

    suspend fun getAllTokensForNetwork(network: Network): List<TokenInformation> {
        return withContext(Dispatchers.IO.limitedParallelism(10)) {
            tokenCache.get("tokens-${network}") {
                erC20Repository.allTokens(network).map {
                    async {
                        try {
                            getTokenInformation(it, network)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }
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

    val tokenInformationCache = Cache.Builder().build<String, TokenInformation>()

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return nativeTokenService.getNativeToken(network)
        }

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