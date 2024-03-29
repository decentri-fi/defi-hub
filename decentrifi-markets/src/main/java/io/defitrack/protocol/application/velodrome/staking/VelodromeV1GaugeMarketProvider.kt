package io.defitrack.protocol.velodrome.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.VelodromeV1GaugeContract
import io.defitrack.protocol.velodrome.contract.VoterContract
import io.defitrack.protocol.velodrome.pooling.VelodromeV1OptimismPoolingMarketProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev1.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV1GaugeMarketProvider(
    private val velodromeV1OptimismPoolingMarketProvider: VelodromeV1OptimismPoolingMarketProvider
) : FarmingMarketProvider() {

    val voter = "0x09236cff45047dbee6b921e00704bed6d6b8cf7e"

    val voterContract = lazyAsync {
        with(getBlockchainGateway()) {
            VoterContract(
                voter
            )
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        velodromeV1OptimismPoolingMarketProvider.getMarkets().parMapNotNull(EmptyCoroutineContext, 12) {
            val gauge = voterContract.await().gauges(it.address)

            if (gauge != "0x0000000000000000000000000000000000000000") {
                try {
                    val contract = with(getBlockchainGateway()) { VelodromeV1GaugeContract(gauge) }

                    val stakedToken = getToken(contract.stakedToken.await())

                    create(
                        name = stakedToken.name + " Gauge",
                        identifier = stakedToken.symbol + "-${gauge}",
                        rewardTokens = contract.getRewardList().map { reward ->
                            getToken(reward)
                        },
                        marketSize = refreshable {
                            getMarketSize(
                                stakedToken,
                                contract.address
                            )
                        },
                        stakedToken = stakedToken,
                        positionFetcher = defaultPositionFetcher(gauge),
                        deprecated = true,
                        type = "velodrome.v1.gauge",
                    )

                } catch (ex: Exception) {
                    logger.error("Failed to fetch gauge market with pooling market {}", it.address, ex)
                    null
                }
            } else {
                null
            }
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V1
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}