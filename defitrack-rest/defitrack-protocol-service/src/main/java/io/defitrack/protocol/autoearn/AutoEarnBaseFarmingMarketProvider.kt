package io.defitrack.protocol.autoearn

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AUTOEARN)
class AutoEarnBaseFarmingMarketProvider : FarmingMarketProvider() {

    final val vaultAddress = "0x04888afae97dc01e337582a2c8d3d232e27273fe"

    val deferredVault = lazyAsync {
        AutoEarnVaultContract(
            getBlockchainGateway(),
            vaultAddress
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> {
        val vault = deferredVault.await()

        return channelFlow {
            val poolInfos = vault.poolInfos2()

            poolInfos.forEachIndexed { index, it ->
                launch {
                    throttled {

                        val underlying = getToken(it.lpToken)
                        val reward = getToken(vault.rewardToken.await())

                        send(
                            create(
                                name = underlying.name,
                                identifier = "$vaultAddress-$index",
                                stakedToken = underlying.toFungibleToken(),
                                rewardTokens = listOf(reward.toFungibleToken()),
                                positionFetcher = PositionFetcher(
                                    vaultAddress,
                                    { user -> vault.autoEarnUserInfoFunction(index, user) }
                                ),
                                claimableRewardFetcher = ClaimableRewardFetcher(
                                    Reward(
                                        reward.toFungibleToken(),
                                        vaultAddress,
                                        vault.pendingFunction(index)
                                    ),
                                    preparedTransaction = selfExecutingTransaction(
                                        vault.getRewardFn(index)
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AUTOEARN
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}