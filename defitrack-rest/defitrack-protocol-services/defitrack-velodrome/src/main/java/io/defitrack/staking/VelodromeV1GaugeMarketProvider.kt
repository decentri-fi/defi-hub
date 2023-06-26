package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.pooling.VelodromeV1OptimismPoolingMarketProvider
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.VelodromeV1GaugeContract
import io.defitrack.protocol.contract.VoterContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class VelodromeV1GaugeMarketProvider(
    private val velodromeV1OptimismPoolingMarketProvider: VelodromeV1OptimismPoolingMarketProvider
) : FarmingMarketProvider() {

    val voter = "0x09236cff45047dbee6b921e00704bed6d6b8cf7e"

    val voterContract by lazy {
        runBlocking {
            VoterContract(
                getBlockchainGateway(),
                voter
            )
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        velodromeV1OptimismPoolingMarketProvider.getMarkets().forEach {
            val gauge = voterContract.gauges(it.address)

            launch {
                throttled {
                    if (gauge != "0x0000000000000000000000000000000000000000") {
                        try {
                            val contract = VelodromeV1GaugeContract(
                                getBlockchainGateway(),
                                gauge
                            )

                            val stakedToken = getToken(contract.stakedToken())

                            val market = create(
                                name = stakedToken.name + " Gauge",
                                identifier = stakedToken.symbol + "-${gauge}",
                                farmType = ContractType.LIQUIDITY_MINING,
                                rewardTokens = contract.getRewardList().map { reward ->
                                    getToken(reward).toFungibleToken()
                                },
                                marketSize = Refreshable.refreshable {
                                    getMarketSize(
                                        stakedToken.toFungibleToken(),
                                        contract.address
                                    )
                                },
                                stakedToken = stakedToken.toFungibleToken(),
                                vaultType = "velodrome-gauge",
                                balanceFetcher = defaultPositionFetcher(gauge),
                                rewardsFinished = true
                            )

                            send(market)
                        } catch (ex: Exception) {
                            logger.error("Failed to fetch gauge market with pooling market {}", it.address, ex)
                        }
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}