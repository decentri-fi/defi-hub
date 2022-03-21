package io.defitrack.erc20

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.ktor.client.*
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

    private lateinit var tokenList: List<TokenInfo>

    companion object {

        val logger: Logger = LoggerFactory.getLogger(this::class.java)

        val NATIVE_WRAP_MAPPING = mapOf(
            Network.ETHEREUM to "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
            Network.AVALANCHE to "0xb31f66aa3c1e785363f0875a1b74e27b85fd66c7",
            Network.POLYGON to "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270",
            Network.ARBITRUM to "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
            Network.BSC to "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
            Network.FANTOM to "0x21be370d5312f44cb42ce377bc9b8a0cef1a4c83"
        )
    }

    @PostConstruct
    fun populateTokens() {
        tokenList = listOf(
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/polygon/quickswap-default.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/polygon/polygon.vetted.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/polygon/polygon.listed.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/ethereum/set.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/arbitrum/tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/fantom/aeb.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/fantom/fantomfinance.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/avalanche/joe.tokenlist.json",
            "https://raw.githubusercontent.com/defitrack/data/master/tokens/bsc/pancakeswap-extended.json",
        ).flatMap {
            fetchFromTokenList(it)
        }
    }

    private fun fetchFromTokenList(url: String) =
        runBlocking {
            val result = client.get<String>(with(HttpRequestBuilder()) {
                url(url)
                this
            })
            val tokens = objectMapper.readValue(
                result,
                TokenListResponse::class.java
            )

            logger.info("imported $url")
            tokens
        }.tokens.mapNotNull { entry ->
            Network.fromChainId(entry.chainId)?.let { network ->
                TokenInfo(
                    name = entry.name,
                    network = network,
                    address = entry.address,
                    logo = entry.logoURI,
                )
            }
        }

    fun allTokens(network: Network): List<TokenInfo> {
        return tokenList.filter { it.network == network }.distinctBy {
            it.address.lowercase()
        }
    }
}