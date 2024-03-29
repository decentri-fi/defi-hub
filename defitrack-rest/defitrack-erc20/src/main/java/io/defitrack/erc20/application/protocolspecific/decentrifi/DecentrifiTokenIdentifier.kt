package io.defitrack.erc20.application.protocolspecific.decentrifi

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.market.FarmingMarketInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.application.protocolspecific.TokenIdentifier
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.port.output.MarketClient
import io.defitrack.port.output.ProtocolClient
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DecentrifiTokenIdentifier(
    private val decentrifiProtocols: ProtocolClient,
    private val markets: MarketClient,
) : TokenIdentifier() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val tokens = Cache.Builder<String, Pair<FungibleTokenInformation, ProtocolInformationDTO>>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
    fun init() = runBlocking {
        decentrifiProtocols.getProtocols().map { proto ->
            try {
                getFarms(proto.slug)
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

    suspend fun getFarms(slug: String): List<FarmingMarketInformationDTO> {
        return markets.getFarmingMarkets(slug)
    }
}