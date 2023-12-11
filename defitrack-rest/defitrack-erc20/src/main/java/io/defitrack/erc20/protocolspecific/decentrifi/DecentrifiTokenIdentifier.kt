package io.defitrack.erc20.protocolspecific.decentrifi

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.FungibleToken
import io.defitrack.erc20.protocolspecific.TokenIdentifier
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO
import io.defitrack.erc20.TokenInformation
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DecentrifiTokenIdentifier(
    private val httpClient: HttpClient
) : TokenIdentifier() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val tokens = Cache.Builder<String, Pair<FungibleToken, ProtocolVO>>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun init() = runBlocking {
        val protocols = getProtocols()
        protocols.map { proto ->
            try {
                getFarms(proto)
                    .filter { farm ->
                        farm.token != null
                    }.forEach { farm ->
                        tokens.put(toIndex(farm.network.toNetwork(), farm.token!!.address), farm.token!! to proto)
                    }
            } catch (ex: Exception) {
                logger.error("Unable to import price for proto ${proto.slug}", ex)
            }
        }
        logger.info("populated ${tokens.asMap().size} tokens from decentrifi farms")
    }


    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return tokens.get(toIndex(token.network, token.address)) != null
    }

    private fun toIndex(network: Network, address: String) =
        "${network.name}-${address.lowercase()}"

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        val (fungible, proto) = tokens.get(toIndex(token.network, token.address))!!
        return TokenInformation(
            name = fungible.name,
            symbol = fungible.symbol,
            address = fungible.address,
            decimals = fungible.decimals,
            totalSupply = token.totalSupply,
            type = TokenType.OTHER,
            protocol = Protocol.valueOf(proto.name),
            network = token.network
        )
    }

    suspend fun getProtocols(): List<ProtocolVO> {
        return httpClient.get("https://api.decentri.fi/protocols").body()
    }

    suspend fun getFarms(protocol: ProtocolVO): List<FarmingMarketVO> {
        val result =
            httpClient.get("https://api.decentri.fi/${protocol.slug}/farming/all-markets")
        return if (result.status.isSuccess())
            result.body()
        else {
            logger.error("Unable to fetch farms for ${protocol.name} ${result.bodyAsText()}")
            emptyList()
        }
    }
}