package io.defitrack.protocol.application.moonwell

import arrow.fx.coroutines.parMap
import io.defitrack.claim.AbstractClaimableMarketProvider
import io.defitrack.claim.ClaimableMarket
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.moonwell.MoonwellUnitRollerContract
import io.defitrack.protocol.moonwell.RewardDistributorContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MOONWELL)
class MoonwellClaimableMarketProvider(
    private val moonwellLendingMarketProvider: MoonwellLendingMarketProvider,
) : AbstractClaimableMarketProvider() {

    val well = lazyAsync {
        erC20Resource.getTokenInformation(Network.BASE, "0xFF8adeC2221f9f4D8dfbAFa6B9a297d17603493D")
    }


    override suspend fun fetchClaimables(): List<ClaimableMarket> =
        with(blockchainGatewayProvider.getGateway(Network.BASE)) {
            val markets = moonwellLendingMarketProvider.getMarkets()
            val comptroller = MoonwellUnitRollerContract(
                "0xfbb21d0380bee3312b33c4353c8936a0f13ef26c"
            )
            val contract = RewardDistributorContract(comptroller.rewardDistributor.await())
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
                        preparedTransaction = selfExecutingTransaction(comptroller::claimReward)
                    ))
                )
            }
        }
}