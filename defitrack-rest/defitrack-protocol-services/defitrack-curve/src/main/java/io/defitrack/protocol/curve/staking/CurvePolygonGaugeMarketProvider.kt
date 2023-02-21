package io.defitrack.protocol.curve.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurveGaugeContract
import io.defitrack.protocol.crv.contract.CurvePolygonGaugeControllerContract
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class CurvePolygonGaugeMarketProvider : FarmingMarketProvider() {
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return coroutineScope {

            val gaugeController = CurvePolygonGaugeControllerContract(
                blockchainGateway = getBlockchainGateway(),
                address = "0xabc000d88f23bb45525e447528dbf656a9d55bf5"
            )

            return@coroutineScope gaugeController.getGaugeAddresses().map { gauge ->
                async {
                    try {
                        val contract = CurveGaugeContract(
                            getBlockchainGateway(),
                            gauge
                        )
                        val rewardTokens = contract.rewardTokens()
                            .filter {
                                it != "0x0000000000000000000000000000000000000000"
                            }
                            .map {
                                getToken(it).toFungibleToken()
                            }

                        val stakedToken = getToken(contract.lpToken())

                        create(
                            identifier = gauge,
                            name = stakedToken.name + " Gauge",
                            stakedToken = stakedToken.toFungibleToken(),
                            rewardTokens = rewardTokens,
                            vaultType = "curve-gauge",
                            marketSize = marketSizeService.getMarketSize(
                                stakedToken.toFungibleToken(), gauge, getNetwork()
                            ),
                            farmType = ContractType.LIQUIDITY_MINING,
                            balanceFetcher = PositionFetcher(
                                gauge,
                                { user ->
                                    getERC20Resource().balanceOfFunction(gauge, user, getNetwork())
                                }
                            ),
                            claimableRewardFetcher = rewardTokens.takeIf { it.isNotEmpty() }?.let {
                                ClaimableRewardFetcher(
                                    address = contract.address,
                                    function = { user ->
                                        contract.getClaimableRewardFunction(
                                            user, rewardTokens.first().address
                                        )
                                    },
                                    preparedTransaction = {
                                        PreparedTransaction(
                                            getNetwork().toVO(),
                                            contract.getClaimRewardsFunction(),
                                            contract.address
                                        )
                                    }
                                )
                            }
                        )
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch curve gauge ${gauge}")
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}