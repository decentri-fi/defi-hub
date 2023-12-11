package io.defitrack.protocol.polygon

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.FungibleToken
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

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
            catch {
                createMarket(share, matic)
            }.mapLeft {
                logger.error("Failed to fetch Polygon Staking market for $share")
            }.getOrNull()
        }
    }

    private suspend fun createMarket(
        share: String,
        matic: FungibleToken
    ): FarmingMarket {
        val shareContract = ValidatorShareProxyContract(
            getBlockchainGateway(),
            share
        )

        return create(
            name = "Polygon Staking",
            identifier = share,
            stakedToken = matic,
            rewardTokens = listOf(matic),
            positionFetcher = PositionFetcher(
                shareContract::getTotalStake
            ),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    matic,
                    shareContract::getLiquidRewards
                ),
                preparedTransaction = selfExecutingTransaction(shareContract::withdrawRewards)
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.POLYGON
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}