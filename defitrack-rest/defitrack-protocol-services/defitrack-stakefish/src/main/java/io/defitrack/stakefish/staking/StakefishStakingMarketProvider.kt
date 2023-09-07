package io.defitrack.stakefish.staking

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stakefish.StakefishFeeRecipientContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import net.bytebuddy.dynamic.scaffold.MethodRegistry.Prepared
import org.springframework.stereotype.Component

@Component
class StakefishStakingMarketProvider : FarmingMarketProvider() {

    private val stakefishFeeRecipient = "0xffee087852cb4898e6c3532e776e68bc68b1143b"

    val stakefishStakingContract by lazy {
        StakefishFeeRecipientContract(
            getBlockchainGateway(),
            stakefishFeeRecipient
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {

        send(
            create(
                name = "Stakefish ETH",
                identifier = "stakefish-eth",
                stakedToken = getToken("0x0").toFungibleToken(),
                rewardTokens = listOf(getToken("0x0").toFungibleToken()),
                vaultType = "stakefish",
                farmType = ContractType.STAKING,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = stakefishFeeRecipient,
                    function = { user ->
                        stakefishStakingContract.getPendingRewardFunction(user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            network = getNetwork().toVO(),
                            function = stakefishStakingContract.claimFunction(user),
                            to = stakefishFeeRecipient,
                        )
                    }
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.STAKEFISH
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}