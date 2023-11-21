package io.defitrack.protocol.curve.staking

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveEthereumGaugeControllerContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
@ConditionalOnCompany(Company.CURVE)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class CurveEthereumGaugeMarketProvider : FarmingMarketProvider() {
    val gaugeControllerAddress = "0x2F50D538606Fa9EDD2B11E2446BEb18C9D5846bB"

    val crvToken = lazyAsync {
        getToken("0xD533a949740bb3306d119CC777fa900bA034cd52")
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val gaugeController = CurveEthereumGaugeControllerContract(
            blockchainGateway = getBlockchainGateway(),
            address = gaugeControllerAddress
        )

        val gaugeAddresses = gaugeController
            .getGaugeAddresses()
        val gaugeTypes = gaugeController.getGaugeTypes(gaugeAddresses)

        return gaugeAddresses
            .zip(gaugeTypes)
            .parMapNotNull(concurrency = 12) { (gauge, gaugeType) ->
                Either.catch {
                    farmingMarket(gauge, gaugeType)
                }.fold(
                    { error ->
                        logger.error("Error fetching gauge $gauge", error)
                        null
                    },
                    { it }
                )
            }
    }

    private suspend fun farmingMarket(gauge: String, gaugeType: BigInteger): FarmingMarket? {
        return when (gaugeType) {
            BigInteger.valueOf(0) -> normalGauge(gauge)
            else -> {
                logger.warn("Unknown gauge type $gaugeType for gauge $gauge")
                null
            }
        }
    }

    private suspend fun CurveEthereumGaugeMarketProvider.normalGauge(gauge: String): FarmingMarket {
        val contract = CurvePolygonGaugeContract(
            getBlockchainGateway(),
            gauge
        )

        val stakedToken = getToken(contract.lpToken())

        return create(
            identifier = gauge,
            name = stakedToken.name + " Gauge",
            stakedToken = stakedToken,
            rewardToken = stakedToken,
            marketSize = Refreshable.refreshable {
                marketSizeService.getMarketSize(
                    stakedToken, gauge, getNetwork()
                ).usdAmount
            },
            positionFetcher = PositionFetcher(
                gauge,
                ERC20Contract.Companion::balanceOfFunction
            ),
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}