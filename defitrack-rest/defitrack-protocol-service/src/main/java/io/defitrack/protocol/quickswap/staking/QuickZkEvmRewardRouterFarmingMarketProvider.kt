package io.defitrack.protocol.quickswap.staking

import arrow.core.nel
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.contract.FeeQlpTrackerContract
import io.defitrack.protocol.quickswap.contract.RewardRouterContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class QuickZkEvmRewardRouterFarmingMarketProvider : FarmingMarketProvider() {

    val rewardRouter = "0x4141b44f0e8b53adcac97d87a3c524d70e5e23b7"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = RewardRouterContract(
            getBlockchainGateway(), rewardRouter
        )

        val feeTrackerContract = FeeQlpTrackerContract(
            getBlockchainGateway(), contract.feeQlpTracker.await()
        )

        val rewardTokens = feeTrackerContract.getAllRewardTokens().map { getToken(it) }
        val stakedToken = getToken(contract.qlp.await())

        return create(
            name = "Fee QLP",
            identifier = rewardRouter,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            claimableRewardFetcher = ClaimableRewardFetcher(
                rewardTokens.map { token ->
                    Reward(
                        token = token,
                        getRewardFunction = feeTrackerContract::claimableFn
                    ) { result, _ ->
                        val addresses = (result[0].value as List<Address>).map { it.value }
                        val amounts = (result[1].value as List<Uint256>).map { it.value }

                        val claimables = addresses.mapIndexed { index, address ->
                            address.lowercase() to amounts[index]
                        }.toMap()

                        claimables.getOrDefault(token.address.lowercase(), BigInteger.ZERO)
                    }
                },
                preparedTransaction = selfExecutingTransaction(contract::claimFn)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}