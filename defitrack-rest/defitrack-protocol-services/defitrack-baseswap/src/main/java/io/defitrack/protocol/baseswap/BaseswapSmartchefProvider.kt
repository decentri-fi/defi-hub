package io.defitrack.protocol.baseswap

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SmartChefContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
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
        getSmartchefAddresses().await().forEach {

            val contract = SmartChefContract(getBlockchainGateway(), it)
            val stakedToken = getToken(contract.stakedToken.await())
            val rewardToken = getToken(contract.rewardToken.await())

            send(
                create(
                    name = stakedToken.name + " earn pool",
                    farmType = ContractType.LIQUIDITY_MINING,
                    rewardTokens = listOf(rewardToken.toFungibleToken()),
                    identifier = it,
                    stakedToken = stakedToken.toFungibleToken(),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        Reward(
                            rewardToken.toFungibleToken(),
                            contract.address,
                            { user ->
                                contract.pendingReward(user)
                            }
                        ),
                        preparedTransaction = { user ->
                            PreparedTransaction(
                                network = getNetwork().toVO(),
                                function = contract.withdraw(),
                                to = contract.address,
                                from = user
                            )
                        }
                    ),
                    balanceFetcher = PositionFetcher(
                        contract.address,
                        { user ->
                            contract.userInfo(user)
                        },
                    ),
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BASESWAP
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}