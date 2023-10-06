package io.defitrack.protocol.moonwell

import ClaimableMarketProvider
import io.defitrack.claimable.ClaimableMarket
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.token.DecentrifiERC20Resource
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.MOONWELL)
class MoonwellClaimableMarketProvider(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val moonwellLendingMarketProvider: MoonwellLendingMarketProvider,
    private val erC20Resource: DecentrifiERC20Resource
) : ClaimableMarketProvider {

    private suspend fun unitRoller(): MoonwellUnitRollerContract {
        return MoonwellUnitRollerContract(
            blockchainGatewayProvider.getGateway(Network.BASE),
            "0xfbb21d0380bee3312b33c4353c8936a0f13ef26c"
        )
    }

    val well = lazyAsync {
        erC20Resource.getTokenInformation(Network.BASE, "0xFF8adeC2221f9f4D8dfbAFa6B9a297d17603493D")
    }


    val deferredComptroller = lazyAsync {
        unitRoller()
    }

    val deferredRewardDistributor = lazyAsync {
        RewardDistributorContract(
            blockchainGatewayProvider.getGateway(Network.BASE),
            deferredComptroller.await().rewardDistributor.await()
        )
    }

    override suspend fun getClaimables(): List<ClaimableMarket> {
        val markets = moonwellLendingMarketProvider.getMarkets()
        return markets.map {

            ClaimableMarket(
                id = "rwrd_${it.id}",
                name = "${it.name} reward",
                network = Network.BASE,
                protocol = Protocol.MOONWELL,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        well.await().toFungibleToken(),
                        deferredRewardDistributor.await().address,
                        { user ->
                            deferredRewardDistributor.await()
                                .getOutstandingRewardsForUserFn(it.metadata["mToken"].toString(), user)
                        },
                        extractAmountFromRewardFunction = { results, _ ->
                            val rewards = results[0].value as List<RewardDistributorContract.Reward>
                            rewards.firstOrNull { r ->
                                r.emissionToken.value as String != "0x0000000000000000000000000000000000000000"
                            }?.amount?.value ?: BigInteger.ZERO
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            Network.BASE.toVO(),
                            unitRoller().claimReward(user),
                            to = deferredComptroller.await().address
                        )
                    }
                )
            )
        }
    }
}