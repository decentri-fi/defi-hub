package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.protocol.crv.CurveEthereumGaugeGraphProvider
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class CurveEthereumFarmingMarketProvider(
    private val curveEthereumGaugeGraphProvider: CurveEthereumGaugeGraphProvider,
    private val erC20Resource: ERC20Resource
) : FarmingMarketService() {

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> = withContext(Dispatchers.IO.limitedParallelism(10)){
        curveEthereumGaugeGraphProvider.getGauges()
            .filter { it.pool != null }
            .map { gauge ->
                async {
                    try {

                        val stakedToken = erC20Resource.getTokenInformation(getNetwork(), gauge.pool!!.lpToken.address)

                        stakingMarket(
                            id = "frm-curve-ethereum-${gauge.address}",
                            name = stakedToken.name + " Gauge",
                            stakedToken = stakedToken.toFungibleToken(),
                            rewardTokens = emptyList(),
                            contractAddress = gauge.address,
                            vaultType = "curve-gauge"
                        )
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch curve gauge ${gauge.address}")
                        null
                    }
                }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}