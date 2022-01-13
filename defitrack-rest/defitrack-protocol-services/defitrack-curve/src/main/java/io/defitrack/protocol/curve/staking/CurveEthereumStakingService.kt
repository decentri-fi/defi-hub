package io.defitrack.protocol.curve.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CrvMainnetGauge
import io.defitrack.protocol.crv.CurveEthereumService
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger
import java.util.*

@Service
class CurveEthereumStakingService(
    private val curveEthereumService: CurveEthereumService,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    objectMapper: ObjectMapper,
) : UserStakingService(erC20Resource, objectMapper) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val gaugeABI by lazy {
        abiResource.getABI("crv/Gauge.json")
    }

    fun getGauge(address: String): CrvMainnetGauge {
        return CrvMainnetGauge(
            ethereumContractAccessor,
            gaugeABI,
            address
        )
    }

    override fun getStakings(address: String): List<StakingElement> {
        val gauges = curveEthereumService.getGauges().filter {
            it.pool != null
        }

        return ethereumContractAccessor.readMultiCall(
            gauges.map { gauge ->
                val gaugeContract = getGauge(gauge.address)
                MultiCallElement(
                    gaugeContract.createFunction(
                        "balanceOf",
                        listOf(address.toAddress()),
                        listOf(
                            TypeReference.create(Uint256::class.java)
                        )
                    ),
                    gaugeContract.address
                )
            }).mapIndexed { index, result ->
            val balance = result[0].value as BigInteger
            val gauge = gauges[index]

            if (balance > BigInteger.ZERO) {
                try {
                    StakingElement(
                        network = getNetwork(),
                        protocol = getProtocol(),
                        name = (gauge.pool?.name ?: "Curve") + " Gauge",
                        id = UUID.randomUUID().toString(),
                        url = "https://etherscan.io/address/" + gauge.address,
                        stakedToken = gauge.pool?.let {
                            vaultStakedToken(
                                it.lpToken.address,
                                balance
                            )
                        },
                        rewardTokens = emptyList(),
                        vaultType = "curve-gauge",
                        contractAddress = gauge.address
                    )
                } catch (ex: Exception) {
                    logger.debug("Something went wrong trying to fetch curve staking")
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