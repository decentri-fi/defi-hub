package io.defitrack.protocol.moonwell

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.ClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MOONWELL)
class MoonwellClaimableMarketProvider(
    private val moonwellLendingMarketProvider: MoonwellLendingMarketProvider,
) : ClaimableMarketProvider() {

    val well = lazyAsync {
        erC20Resource.getTokenInformation(Network.BASE, "0xFF8adeC2221f9f4D8dfbAFa6B9a297d17603493D")
    }


    val deferredComptroller = lazyAsync {
        MoonwellUnitRollerContract(
            blockchainGatewayProvider.getGateway(Network.BASE),
            "0xfbb21d0380bee3312b33c4353c8936a0f13ef26c"
        )
    }

    val deferredRewardDistributor = lazyAsync {
        RewardDistributorContract(
            blockchainGatewayProvider.getGateway(Network.BASE),
            deferredComptroller.await().rewardDistributor.await()
        )
    }

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val markets = moonwellLendingMarketProvider.getMarkets()
        val contract = deferredRewardDistributor.await()
        return markets.parMap(concurrency = 8) {
            ClaimableMarket(
                id = "rwrd_${it.id}",
                name = "${it.name} reward",
                network = Network.BASE,
                protocol = Protocol.MOONWELL,
                claimableRewardFetchers = listOf(ClaimableRewardFetcher(
                    Reward(
                        well.await(),
                        contract.getOutstandingRewardsForUserFn(it.metadata["mToken"].toString()),
                        extractAmountFromRewardFunction = { results, _ ->
                            val rewards = results[0].value as List<RewardDistributorContract.Reward>
                            rewards.firstOrNull { r ->
                                r.emissionToken.value as String != "0x0000000000000000000000000000000000000000"
                            }?.amount?.value ?: BigInteger.ZERO
                        }
                    ),
                    preparedTransaction = selfExecutingTransaction(deferredComptroller.await()::claimReward)
                ))
            )
        }
    }
}