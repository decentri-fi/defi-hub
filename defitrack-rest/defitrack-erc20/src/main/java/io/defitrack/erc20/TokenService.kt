package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.pool.LPtokenService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.Token
import io.defitrack.protocol.staking.TokenType
import io.defitrack.token.ERC20Resource
import io.defitrack.token.domain.ERC20Information
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class TokenService(
    val erc20Resource: ERC20Resource,
    private val LPtokenService: LPtokenService
) {

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

        }
        isKyberDMMLP(symbol) -> {
            TokenType.KYBER
        }
        else -> {
            TokenType.SINGLE
        }
    }

    val tokenCache = Cache.Builder().build<String, Token>()

    fun getTokenInformation(address: String, network: Network): Token = runBlocking {
        tokenCache.get("${address}-${network}") {
            val token = erc20Resource.getERC20(network, address)
            when {
                (token.symbol) == "SLP" -> {
                    fromLP(Protocol.SUSHISWAP, network, token)
                }
                (token.symbol) == "UNI-V2" -> {
                    fromLP(if (network == Network.POLYGON) Protocol.QUICKSWAP else Protocol.QUICKSWAP, network, token)
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
                    fromLP(Protocol.HOP, network, token)
                }
                else -> {
                    Token(
                        name = token.name,
                        symbol = token.symbol,
                        address = token.address,
                        decimals = token.decimals,
                        totalSupply = BigInteger.ZERO,
                        type = TokenType.SINGLE,
                        token0 = null,
                        token1 = null
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

    fun fromLP(protocol: Protocol, network: Network, erc20: ERC20Information): Token {
        val lp = LPtokenService.getLP(network, erc20.address)

        val token0 = getTokenInformation(
            lp.token0, network
        )
        val token1 = getTokenInformation(
            lp.token1, network
        )

        return Token(
            name = "${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            token0 = token0,
            token1 = token1,
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply,
            type = getType(lp.symbol),
            protocol = protocol
        )
    }
}