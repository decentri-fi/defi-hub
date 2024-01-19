package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.TypeUtils.Companion.address
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

    private val balAddress = "0x120ef59b80774f02211563834d8e3b72cb1649d6"

    val l2PseudoMinterAddress = "0x475D18169BE8a89357A9ee3Ab00ca386d20fA229"

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
        val bal = getToken(balAddress)
        //val l2BalancerPseudoMinter = L2BalancerPseudoMinterContract(getBlockchainGateway(), l2PseudoMinterAddress)

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
                } + bal


                create(
                    identifier = gaugeAddress,
                    name = lp.name + " gauge",
                    stakedToken = getToken(gaugecontract.getStakedToken()),
                    rewardTokens = rewards,
                    positionFetcher = PositionFetcher(
                        gaugecontract::workingBalance
                    ) {
                        val balance = it[0].value as BigInteger
                        if (balance > BigInteger.ZERO) {
                            val ratiod =
                                balance.toBigDecimal()
                                    .dividePrecisely(gaugecontract.workingSupply.await().toBigDecimal())
                            val normalized = lp.totalSupply.toBigDecimal().times(ratiod)
                            Position(
                                normalized.toBigInteger(),
                                balance
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
                        gaugecontract.exitPosition(it.amount)
                    },
                    claimableRewardFetchers = listOf(
                        ClaimableRewardFetcher(
                            rewards = rewards.map { reward ->
                                Reward(
                                    reward,
                                    gaugecontract.getClaimableRewardFunction(reward.address)
                                )
                            },
                            preparedTransaction = selfExecutingTransaction(gaugecontract::getClaimRewardsFunction)
                        )
                        /*, ClaimableRewardFetcher(
                            reward = Reward(
                                bal,
                                gaugeAddress,
                                getRewardFunction = gaugecontract::integrateFractionFn,
                                extractAmountFromRewardFunction = { retVal, user ->
                                    val balance = retVal[0].value as BigInteger
                                    if (balance > BigInteger.ZERO) {
                                        val minted = l2BalancerPseudoMinter.minted(user, gaugeAddress)
                                        balance - minted
                                    } else {
                                        BigInteger.ZERO

                                    }
                                }
                            ),
                            preparedTransaction = selfExecutingTransaction { user ->
                                val balance = gaugecontract.integrateFraction(user)
                                val minted = l2BalancerPseudoMinter.minted(user, gaugeAddress)
                                val amount = balance - minted
                                l2BalancerPseudoMinter.mint(amount.abs(), user)
                            }
                        )*/
                    ),
                )
            } catch (ex: Exception) {
                logger.error("Error fetching gauge", ex)
                logger.debug(ex.stackTraceToString())
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