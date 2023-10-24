package io.defitrack.protocol.polygon

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
@ConditionalOnCompany(Company.POLYGON)
class PolygonValidatorShareProxyMarketProvider : FarmingMarketProvider() {

    val polygonStakingMarket = "0x5e3ef299fddf15eaa0432e6e66473ace8c13d908"

    val maticAddress = "0x7d1afa7b718fb893db30a3abc0cfc608aacfebb0"


    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val polygonStakingContract = PolygonStakingContract(
            getBlockchainGateway(),
            polygonStakingMarket
        )

        val matic = getToken(maticAddress)

        return polygonStakingContract.getStakingShareProviders().parMapNotNull(concurrency = 8) { share ->
            Either.catch {

                val shareContract = ValidatorShareProxyContract(
                    getBlockchainGateway(),
                    share
                )

                create(
                    name = "Polygon Staking",
                    identifier = share,
                    stakedToken = matic,
                    rewardTokens = listOf(matic),
                    positionFetcher = PositionFetcher(
                        share,
                        shareContract::getTotalStake
                    ),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        Reward(
                            matic,
                            share,
                            shareContract::getLiquidRewards
                        ),
                        preparedTransaction = selfExecutingTransaction(shareContract::withdrawRewards)
                    )
                )
            }.mapLeft {
                logger.error("Failed to fetch Polygon Staking market for $share")
            }.getOrNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYGON
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}