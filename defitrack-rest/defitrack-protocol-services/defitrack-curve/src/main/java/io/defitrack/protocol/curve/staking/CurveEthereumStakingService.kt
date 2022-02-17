package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.util.*

@Service
class CurveEthereumStakingService(
    private val curveEthereumService: CurveEthereumService,
    private val ethereumContractAccessor: EthereumContractAccessor,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun getStakings(address: String): List<StakingElement> {
        val gauges = curveEthereumService.getGauges().filter {
            it.pool != null
        }

        return erC20Resource.getBalancesFor(address, gauges.map { it.address }, ethereumContractAccessor)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val gauge = gauges[index]
                    val lpToken = erC20Resource.getTokenInformation(getNetwork(), gauge.pool!!.lpToken.address)

                    try {
                        StakingElement(
                            network = getNetwork(),
                            protocol = getProtocol(),
                            name = (gauge.pool?.name ?: "Curve") + " Gauge",
                            id = UUID.randomUUID().toString(),
                            url = "https://etherscan.io/address/" + gauge.address,
                            stakedToken = stakedToken(
                                lpToken.address,
                                lpToken.type
                            ),
                            rewardTokens = emptyList(),
                            vaultType = "curve-gauge",
                            contractAddress = gauge.address,
                            amount = balance

                        )
                    } catch (ex: Exception) {
                        logger.debug("Something went wrong trying to fetch curve staking", ex.message)
                        null
                    }
                } else null
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}