package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.pooling.VelodromeOptimismPoolingMarketProvider
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.VelodromeGaugeContract
import io.defitrack.protocol.contract.VoterContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class VelodromeGaugeMarketProvider(
    private val velodromeOptimismPoolingMarketProvider: VelodromeOptimismPoolingMarketProvider
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

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        velodromeOptimismPoolingMarketProvider.getMarkets().map {
            val gauge = voterContract.gauges(it.address)

            async {
                if (gauge == "0x0000000000000000000000000000000000000000")
                    return@async null

                try {
                    val contract = VelodromeGaugeContract(
                        getBlockchainGateway(),
                        gauge
                    )

                    val stakedToken = getToken(contract.stakedToken())
                    create(
                        name = stakedToken.name + " Gauge",
                        identifier = stakedToken.symbol + "-gauge",
                        farmType = ContractType.LIQUIDITY_MINING,
                        rewardTokens = contract.getRewardList().map { reward ->
                            getToken(reward).toFungibleToken()
                        },
                        marketSize = getMarketSize(
                            stakedToken.toFungibleToken(),
                            contract.address
                        ),
                        stakedToken = stakedToken.toFungibleToken(),
                        vaultType = "velodrome-gauge",
                        balanceFetcher = defaultPositionFetcher(gauge)
                    )
                } catch (ex: Exception) {
                    logger.error("Failed to fetch gauge market with pooling market {}", it.address, ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}