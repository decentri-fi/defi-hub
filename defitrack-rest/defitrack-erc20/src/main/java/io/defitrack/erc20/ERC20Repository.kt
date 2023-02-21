package io.defitrack.erc20

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ERC20Repository(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    private lateinit var tokenList: Map<Network, List<String>>

    companion object {

        val logger: Logger = LoggerFactory.getLogger(this::class.java)

        val NATIVE_WRAP_MAPPING = mapOf(
            Network.ETHEREUM to "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            Network.AVALANCHE to "0xb31f66aa3c1e785363f0875a1b74e27b85fd66c7",
            Network.POLYGON to "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270",
            Network.ARBITRUM to "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
            Network.BINANCE to "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
            Network.FANTOM to "0x21be370d5312f44cb42ce377bc9b8a0cef1a4c83"
        )
    }

    @PostConstruct
    fun populateTokens() = runBlocking {
        tokenList = listOf(
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/quickswap-default.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/polygon.vetted.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/polygon.listed.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/ethereum/set.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/ethereum/uniswap-default.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/arbitrum/tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/fantom/aeb.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/fantom/fantomfinance.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/avalanche/joe.tokenlist.json",
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/bsc/pancakeswap-extended.json",
        ).flatMap {
            fetchFromTokenList(it)
        }.groupBy({
            it.first
        }, {
            it.second
        })
    }

    private suspend fun fetchFromTokenList(url: String): List<Pair<Network, String>> {
        val result: String = client.get(with(HttpRequestBuilder()) {
            url(url)
            this
        }).body()
        val tokens = objectMapper.readValue(
            result,
            TokenListResponse::class.java
        )

        logger.info("imported $url")

        return tokens.tokens.mapNotNull { entry ->
            Network.fromChainId(entry.chainId)?.let { network ->
                network to entry.address
            }
        }
    }

    fun allTokens(network: Network): List<String> {
        val addresses = tokenList[network] ?: emptyList()
        return addresses.distinctBy {
            it.lowercase()
        }
    }
}