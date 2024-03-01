package io.defitrack.protocol.application.autoearn

import arrow.core.Either
import arrow.fx.coroutines.parMap
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.autoearn.AutoEarnVaultContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.AUTOEARN)
class AutoEarnBaseFarmingMarketProvider : FarmingMarketProvider() {

    final val vaultAddress = "0x04888afae97dc01e337582a2c8d3d232e27273fe"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val vault = with(getBlockchainGateway()) { AutoEarnVaultContract(vaultAddress)}

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
                    type = "autoearn.vault",
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