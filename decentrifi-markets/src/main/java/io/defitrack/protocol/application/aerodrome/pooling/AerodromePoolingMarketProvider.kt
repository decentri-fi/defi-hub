package io.defitrack.protocol.application.aerodrome.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.LPTokenContract
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
        ).allPools()
            .map {
                createContract {
                    LPTokenContract(it)
                }
            }.resolve()
            .parMap(concurrency = 12) { contract ->
                val token0 = getToken(contract.token0.await())
                val token1 = getToken(contract.token1.await())
                try {
                    val breakdown = refreshable {
                        breakdownOf(contract.address, token0, token1)
                    }

                    create(
                        identifier = contract.address,
                        positionFetcher = defaultPositionFetcher(contract.address),
                        address = contract.address,
                        name = breakdown.get().joinToString("/") { it.token.name },
                        symbol = breakdown.get().joinToString("/") { it.token.symbol },
                        breakdown = breakdown,
                        totalSupply = contract.totalDecimalSupply(),
                        deprecated = false
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market ${contract.address}: {}", ex.message)
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