package io.defitrack.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.gmx.StakedGMXContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class StakedBonusFeeStakingMarketProvider : FarmingMarketProvider() {

    private val stakedGMX = "0xd2d1162512f927a7e282ef43a362659e4f2a728f"
    private val bonusGMX = "0x35247165119b69a40edd5304969560d0ef486921"
    private val stakedBonusGMX = "0x4d268a7d4c16ceb5a606c173bd974984343fea13"
    private val gmx = "0xfc5a1a6eb076a2c7ad06ed22c90d7e710e35ad0a"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = StakedGMXContract(getBlockchainGateway(), stakedGMX)

        send(
            create(
                name = "Staked GMX",
                identifier = "staked-gmx-$stakedGMX",
                stakedToken = getToken(gmx).toFungibleToken(),
                rewardTokens = listOf(
                    getToken(contract.rewardToken.await()).toFungibleToken()
                ),
                vaultType = "staked-gmx",
                marketSize = Refreshable.refreshable { BigDecimal.ZERO },
                farmType = ContractType.STAKING,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = stakedGMX,
                    function = { user ->
                        contract.claimableFn(user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            getNetwork().toVO(),
                            contract.claimFn(user),
                            contract.address
                        )
                    }
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.GMX
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}