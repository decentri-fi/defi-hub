package io.defitrack.protocol.baseswap

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.BASESWAP)
class BaseswapSmartchefProvider : FarmingMarketProvider() {

    val event = Event(
        "NewSmartChefContract",
        listOf(address(true))
    )

    fun getSmartchefAddresses() = lazyAsync {
        val eventsAsEthLog = getBlockchainGateway().getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf("0xc9ee54147445f1c1c23f52183c95456e69a92989"),
                topic = "0xe0d103a92c6ff6c4aceb49d436f4028e0deb4884fdbcb9f32b03045eabb44a6c",
                fromBlock = BigInteger.valueOf(2144391L),
            )
        )

        eventsAsEthLog.map {
            event.extract<String>(it, true, 0)
        }
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        getSmartchefAddresses().await().parMapNotNull(concurrency = 8) {
            catch {
                createMarket(it)
            }.mapLeft {
                logger.error("Failed to create market for $it", it)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(
        it: String,
    ): FarmingMarket {
        val contract = SmartChefContract(getBlockchainGateway(), it)
        val stakedToken = getToken(contract.stakedToken.await())
        val rewardToken = getToken(contract.rewardToken.await())

        return create(
            name = stakedToken.name + " earn pool",
            rewardToken = rewardToken,
            identifier = it,
            stakedToken = stakedToken,
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract::pendingReward
                ),
                preparedTransaction = selfExecutingTransaction(contract::withdraw)
            ),
            positionFetcher = PositionFetcher(
                contract::userInfo,
            ),
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.BASESWAP
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}