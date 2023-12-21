package io.defitrack.protocol.autoearn

import arrow.core.Either
import arrow.fx.coroutines.parMap
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AUTOEARN)
class AutoEarnBaseFarmingMarketProvider : FarmingMarketProvider() {

    final val vaultAddress = "0x04888afae97dc01e337582a2c8d3d232e27273fe"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val vault = AutoEarnVaultContract(
            getBlockchainGateway(),
            vaultAddress
        )

        val poolInfos = vault.poolInfos2()

        poolInfos.parMap(concurrency = 8) { poolInfo ->
            Either.catch {
                val index = poolInfos.indexOf(poolInfo)
                val underlying = getToken(poolInfo.lpToken)
                val rewardToken = getToken(vault.rewardToken.await())

                create(
                    name = underlying.name,
                    identifier = "$vaultAddress-$index",
                    stakedToken = underlying,
                    rewardToken = rewardToken,
                    positionFetcher = PositionFetcher(
                        vault.autoEarnUserInfoFunction(index)
                    ),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        Reward(
                            rewardToken,
                            vault.pendingFunction(index)
                        ),
                        preparedTransaction = selfExecutingTransaction(
                            vault.getRewardFn(index)
                        )
                    )
                )
            }.mapLeft {
                logger.info("Unable to fetch market for {}", poolInfo.lpToken)
            }
        }.mapNotNull {
            it.getOrNull()
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AUTOEARN
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}