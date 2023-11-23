package io.defitrack.protocol.spark

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SPARK)
class SparkLendingMarketProvider : LendingMarketProvider() {

    val pool = lazyAsync {
        io.defitrack.protocol.spark.PoolContract(
            getBlockchainGateway(),
            "0xC13e21B648A5Ee794902342038FF3aDAB66BE987"
        )
    }

    override suspend fun fetchMarkets(): List<LendingMarket> {
        val poolContract = pool.await()
        return coroutineScope {
            poolContract.reservesList().map {
                async {
                    throttled {
                        try {

                            val reserveData = poolContract.getReserveData(it)
                            val aToken = getToken(reserveData.aTokenAddress)
                            val underlying = getToken(it)

                            create(
                                identifier = aToken.address,
                                name = "Spark " + aToken.name,
                                token = underlying.toFungibleToken(),
                                poolType = "spark",
                                positionFetcher = PositionFetcher(
                                    aToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
                                ),
                                marketToken = aToken.toFungibleToken(),
                                totalSupply = Refreshable.refreshable(aToken.totalSupply.asEth(aToken.decimals)) {
                                    getToken(aToken.address).totalSupply.asEth(aToken.decimals)
                                }
                            )
                        } catch (ex: Exception) {
                            logger.error("Unable to fetch V3 Lending market with address $it", ex)
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override fun getProtocol() = Protocol.SPARK

    override fun getNetwork() = Network.ETHEREUM
}