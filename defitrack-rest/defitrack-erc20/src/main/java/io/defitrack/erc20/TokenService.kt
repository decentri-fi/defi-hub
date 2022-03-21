package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.logo.LogoService
import io.defitrack.nativetoken.NativeTokenService
import io.defitrack.pool.LPtokenService
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class TokenService(
    private val erc20Service: ERC20Service,
    private val erC20Repository: ERC20Repository,
    private val LPtokenService: LPtokenService,
    private val hopTokenService: HopTokenService,
    private val nativeTokenService: NativeTokenService,
    private val logoService: LogoService
) {


    fun getAllTokensForNetwork(network: Network): List<TokenInformation> {
        return erC20Repository.allTokens(network).map {
            getTokenInformation(it.address, network)
        }
    }

    fun getType(symbol: String): TokenType = when {
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
        isHopLp(symbol) -> {
            TokenType.HOP
        }
        isKyberDMMLP(symbol) -> {
            TokenType.KYBER
        }
        else -> {
            TokenType.SINGLE
        }
    }

    val tokenInformationCache = Cache.Builder().build<String, TokenInformation>()

    fun getTokenInformation(address: String, network: Network): TokenInformation = runBlocking {

        if (address == "0x0" || address.lowercase() == "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee") {
            return@runBlocking nativeTokenService.getNativeToken(network)
        }

        tokenInformationCache.get("${address}-${network}") {
            val token = erc20Service.getERC20(network, address)
            when {
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
                (token.symbol == "SPIRIT-LP") -> {
                    fromLP(Protocol.SPIRITSWAP, network, token)
                }
                isKyberDMMLP(token.symbol) -> {
                    fromLP(Protocol.DMM, network, token)
                }
                isHopLp(token.symbol) -> {
                    hopTokenService.getTokenInformation(token.address, network)
                }
                else -> {
                    TokenInformation(
                        logo = logoService.generateLogoUrl(network, address),
                        name = token.name,
                        symbol = token.symbol,
                        address = token.address,
                        decimals = token.decimals,
                        totalSupply = BigInteger.ZERO,
                        type = TokenType.SINGLE,
                        tokenInformation0 = null,
                        tokenInformation1 = null
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

    fun fromLP(protocol: Protocol, network: Network, erc20: ERC20): TokenInformation {
        val lp = LPtokenService.getLP(network, erc20.address)

        val token0 = getTokenInformation(
            lp.token0, network
        )
        val token1 = getTokenInformation(
            lp.token1, network
        )

        return TokenInformation(
            name = "${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            tokenInformation0 = token0,
            tokenInformation1 = token1,
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply,
            type = getType(lp.symbol),
            protocol = protocol
        )
    }
}