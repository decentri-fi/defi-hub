package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.protocol.crv.CurveEthereumGaugeGraphProvider
import io.defitrack.token.ERC20Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class CurveEthereumFarmingPositionProvider(
    private val curveEthereumGaugeGraphProvider: CurveEthereumGaugeGraphProvider,
    erC20Resource: ERC20Resource,
) : FarmingPositionProvider(erC20Resource) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getStakings(address: String): List<FarmingPosition> {
        val gauges = curveEthereumGaugeGraphProvider.getGauges().filter {
            it.pool != null
        }

        return erC20Resource.getBalancesFor(
            address,
            gauges.map { it.address },
            getNetwork()
        ).mapIndexed { index, balance ->
            if (balance > BigInteger.ZERO) {
                val gauge = gauges[index]
                val lpToken = erC20Resource.getTokenInformation(getNetwork(), gauge.pool!!.lpToken.address)

                try {
                    stakingElement(
                        vaultName = (gauge.pool?.name ?: "Curve") + " Gauge",
                        id = "curve-ethereum-${gauge.address}",
                        stakedToken = lpToken.toFungibleToken(),
                        rewardTokens = emptyList(),
                        vaultType = "curve-gauge",
                        vaultAddress = gauge.address,
                        amount = balance
                    )
                } catch (ex: Exception) {
                    logger.error("Something went wrong trying to fetch curve staking", ex)
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