package io.defitrack.protocol.application.aerodrome.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AERODROME)
class AerodromePoolingMarketProvider : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x420DD381b31aEf6683db6B902084cB0FFECe40Da"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        PoolFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = poolFactoryAddress
        ).allPools().parMap(concurrency = 12) {
            val poolingToken = getToken(it)
            val tokens = poolingToken.underlyingTokens

            try {
                val breakdown = refreshable {
                    breakdownOf(poolingToken.address, tokens[0], tokens[1])
                }

                create(
                    identifier = it,
                    positionFetcher = defaultPositionFetcher(poolingToken.address),
                    address = it,
                    name = poolingToken.name,
                    breakdown = breakdown,
                    symbol = poolingToken.symbol,
                    totalSupply = refreshable {
                        getToken(it).totalDecimalSupply()
                    },
                    type = "pool",
                    deprecated = false
                )
            } catch (ex: Exception) {
                logger.error("Error while fetching pooling market $it: {}", ex.message)
                null
            }
        }.filterNotNull()
            .forEach {
                send(it)
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AERODROME
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}