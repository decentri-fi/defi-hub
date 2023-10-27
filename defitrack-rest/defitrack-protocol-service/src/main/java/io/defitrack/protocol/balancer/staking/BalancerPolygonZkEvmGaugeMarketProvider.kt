package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeZkEvmContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonZkEvmGaugeMarketProvider : FarmingMarketProvider() {

    private val factory = "0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95"

    val gaugeCreatedEvent = Event(
        "GaugeCreated", listOf(address(true))
    )

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val logs = getBlockchainGateway().getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(factory),
                topic = "0xaa98436d09d130af48de49867af8b723bbbebb0d737638b5fe8f1bf31bbb71c0",
                fromBlock = BigInteger.valueOf(203653L)
            )
        )
        return logs.mapNotNull {
            try {
                val log = it.get()
                val gaugeAddress: String = gaugeCreatedEvent.extract(log, true, 0)
                val gaugecontract = BalancerGaugeZkEvmContract(
                    getBlockchainGateway(),
                    gaugeAddress
                )

                val lp = getToken(gaugecontract.lpToken.await())
                val rewards = gaugecontract.rewardTokens().map { rewardToken ->
                    getToken(rewardToken)
                }

                create(
                    identifier = gaugeAddress,
                    name = lp.name + " gauge",
                    stakedToken = getToken(gaugecontract.getStakedToken()).toFungibleToken(),
                    rewardTokens = rewards.map(TokenInformationVO::toFungibleToken),

                    positionFetcher = PositionFetcher(
                        gaugecontract.address,
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
                        } else {
                            Position(
                                BigInteger.ZERO,
                                BigInteger.ZERO
                            )
                        }
                    },
                    metadata = mapOf("address" to gaugeAddress),
                    exitPositionPreparer = prepareExit {
                        PreparedExit(
                            function = gaugecontract.exitPosition(it.amount),
                            to = gaugecontract.address,
                        )
                    },
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        rewards = rewards.map {
                            Reward(
                                it.toFungibleToken(),
                                gaugeAddress,
                                gaugecontract.getClaimableRewardFunction(it.address)
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
        return Network.POLYGON_ZKEVM
    }
}