package io.defitrack.protocol.balancer.staking

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.event.EventDecoder.Companion.getIndexedParameter
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerGaugeContract
import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonZkEvmGaugeMarketProvider(
) : FarmingMarketProvider() {

    private val factory = "0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95"

    val gaugeCreatedEvent = Event(
        "GaugeCreated", listOf(address(true))
    )

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val logs = getBlockchainGateway().getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf("0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95"),
                topic = "0xaa98436d09d130af48de49867af8b723bbbebb0d737638b5fe8f1bf31bbb71c0",
                fromBlock = BigInteger.valueOf(203653L)
            )
        )
        return logs.map {
            val log = it.get()
            val gaugeAddress: String = gaugeCreatedEvent.extract(log, true, 0)

            val gaugecontract = BalancerGaugeContract(
                getBlockchainGateway(),
                gaugeAddress
            )

            create(
                identifier = gaugeAddress,
                name = "unknown gauge",
                stakedToken = getToken(gaugecontract.getStakedToken()).toFungibleToken(),
                rewardTokens = getRewardTokens(
                    gaugecontract
                ).map { reward ->
                    reward.toFungibleToken()
                },
                balanceFetcher = PositionFetcher(
                    gaugecontract.address,
                    { user -> ERC20Contract.balanceOfFunction(user) }
                ),
                farmType = ContractType.STAKING,
                metadata = mapOf("address" to gaugeAddress),
                exitPositionPreparer = prepareExit {
                    PreparedExit(
                        function = gaugecontract.exitPosition(it.amount),
                        to = gaugecontract.address,
                    )
                }
            )
        }
    }

    private suspend fun getRewardTokens(balancerGaugeContract: BalancerGaugeContract): List<TokenInformationVO> {
        return (0..3).mapNotNull {
            try {
                val rewardToken = balancerGaugeContract.getRewardToken(it)
                if (rewardToken != "0x0000000000000000000000000000000000000000") {
                    getToken(rewardToken)
                } else {
                    null
                }
            } catch (ex: Exception) {
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