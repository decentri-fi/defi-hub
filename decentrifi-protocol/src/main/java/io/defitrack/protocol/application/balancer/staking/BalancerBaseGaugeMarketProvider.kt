package io.defitrack.protocol.application.balancer.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.abi.TypeUtils
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeZkEvmContract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerBaseGaugeMarketProvider : FarmingMarketProvider() {

    val factory = "0xb1a4FE1C6d25a0DDAb47431A92A723dd71d9021f"

    val gaugeCreatedEvent = Event(
        "GaugeCreated", listOf(TypeUtils.address(true))
    )

    override suspend fun fetchMarkets(): List<FarmingMarket> = with(getBlockchainGateway()) {
        val logs = getBlockchainGateway().getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(factory),
                topic = "0xaa98436d09d130af48de49867af8b723bbbebb0d737638b5fe8f1bf31bbb71c0",
                fromBlock = BigInteger.valueOf(203653L)
            )
        )
        return logs.parMapNotNull(concurrency = 12) {
            try {
                val log = it.get()
                val gaugeAddress: String = gaugeCreatedEvent.extract(log, true, 0)
                val gaugecontract = BalancerGaugeZkEvmContract(gaugeAddress)

                val lp = getToken(gaugecontract.lpToken.await())
                val rewards = gaugecontract.rewardTokens().map { rewardToken ->
                    getToken(rewardToken)
                }

                create(
                    identifier = gaugeAddress,
                    name = lp.name + " gauge",
                    stakedToken = getToken(gaugecontract.getStakedToken()),
                    rewardTokens = rewards,
                    positionFetcher = PositionFetcher(
                        gaugecontract::workingBalance
                    ) {
                        val bal = it[0].value as BigInteger
                        if (bal > BigInteger.ZERO) {
                            val ratiod =
                                bal.toBigDecimal().dividePrecisely(gaugecontract.workingSupply.await().toBigDecimal())
                            val normalized = lp.totalSupply.toBigDecimal().times(ratiod)
                            Position(
                                bal,
                                normalized.toBigInteger()
                            )
                        } else Position.ZERO
                    },
                    metadata = mapOf("address" to gaugeAddress),
                    exitPositionPreparer = prepareExit {
                        gaugecontract.exitPosition(it.amount)
                    },
                    type = "balancer.gauge",
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        rewards = rewards.map { token ->
                            Reward(
                                token,
                                gaugecontract.getClaimableRewardFunction(token.address)
                            )
                        },
                        preparedTransaction = selfExecutingTransaction(gaugecontract::getClaimRewardsFunction)
                    ),
                )
            } catch (ex: Exception) {
                logger.error("Error fetching gauge", ex)
                null
            }
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }

}