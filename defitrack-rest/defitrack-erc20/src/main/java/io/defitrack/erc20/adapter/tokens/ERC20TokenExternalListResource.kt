package io.defitrack.erc20.adapter.tokens

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.erc20.application.repository.ERC20TokenListResource
import io.defitrack.erc20.application.repository.TokenListResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ERC20TokenExternalListResource (
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) : ERC20TokenListResource {

    val logger: Logger = LoggerFactory.getLogger(ERC20TokenListResource::class.java)

    private var tokenList: Map<Network, List<String>> = emptyMap()

    suspend fun populateTokens() {
        tokenList = listOf(
            "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/ethereum/uniswap-default.tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/quickswap-default.tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/polygon.vetted.tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon/polygon.listed.tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/ethereum/extendedtokens.uniswap.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/arbitrum/tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/optimism/optimism.tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/polygon-zkevm/tokenlist.json",
            //         "https://raw.githubusercontent.com/decentri-fi/data/master/tokens/base/tokenlist.json",
        ).flatMap {
            try {
                fetchFromTokenList(it)
            } catch (exception: Exception) {
                logger.error("failed to fetch $it", exception)
                emptyList()
            }
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

        return tokens.tokens.flatMap { entry ->
            entry.getAddressesToNetworks().map {
                it.key to it.value
            }
        }
    }

    override fun allTokens(network: Network): List<String> {
        val addresses = tokenList[network] ?: emptyList()
        return addresses.distinctBy {
            it.lowercase()
        }
    }
}



val NATIVE_WRAP_MAPPING = mapOf(
    Network.ETHEREUM to "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2",
    Network.OPTIMISM to "0x4200000000000000000000000000000000000006",
    Network.POLYGON to "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270",
    Network.ARBITRUM to "0x82af49447d8a07e3bd95bd0d56f35241523fbab1",
    Network.POLYGON_ZKEVM to "0x4f9a0e7fd2bf6067db6994cf12e4495df938e6e9",
    Network.BASE to "0x4200000000000000000000000000000000000006"
)