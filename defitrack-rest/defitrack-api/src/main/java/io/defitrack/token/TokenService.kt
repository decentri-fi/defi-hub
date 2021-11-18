package io.defitrack.token

import io.defitrack.pool.LPtokenService
import io.defitrack.token.domain.ERC20Information
import io.defitrack.common.network.Network
import io.defitrack.protocol.staking.LpToken
import io.defitrack.protocol.staking.SingleToken
import io.defitrack.protocol.staking.Token
import io.defitrack.protocol.staking.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

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
                    fromLP("Sushi", network, token)
                }
                (token.symbol) == "UNI-V2" -> {
                    fromLP(if (network == Network.POLYGON) "Quickswap" else "Uniswap", network, token)
                }
                (token.symbol) == "WLP" -> {
                    fromLP("Wault", network, token)
                }
                (token.symbol) == "spLP" -> {
                    fromLP("Spooky", network, token)
                }
                (token.symbol == "DFYNLP") -> {
                    fromLP("Dfyn", network, token)
                }
                (token.symbol == "SPIRIT-LP") -> {
                    fromLP("Spirit", network, token)
                }
                isKyberDMMLP(token.symbol) -> {
                    fromLP("DMM-LP", network, token)
                }
                else -> {
                    SingleToken(
                        name = token.name,
                        symbol = token.symbol,
                        address = token.address,
                        decimals = token.decimals
                    )
                }
            }
        }
    }

    private fun isKyberDMMLP(symbol: String): Boolean {
        return symbol.startsWith("DMM-LP")
    }

    fun fromLP(provider: String, network: Network, erc20: ERC20Information): LpToken {
        val lp = LPtokenService.getLP(network, erc20.address)

        val token0 = getTokenInformation(
            lp.token0, network
        )
        val token1 = getTokenInformation(
            lp.token1, network
        )

        return LpToken(
            name = "$provider ${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            token0 = token0,
            token1 = token1,
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply,
            type = getType(lp.symbol)
        )
    }
}