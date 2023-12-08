package io.defitrack.protocol.spark

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SPARK)
class SparkLendingMarketProvider : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {

        val poolContract = PoolContract(
            getBlockchainGateway(),
            "0xC13e21B648A5Ee794902342038FF3aDAB66BE987"
        )

        return poolContract.reservesList().parMapNotNull(concurrency = 8) {
            try {
                val reserveData = poolContract.getReserveData(it)
                val aToken = getToken(reserveData.aTokenAddress)
                val underlying = getToken(it)

                create(
                    identifier = aToken.address,
                    name = aToken.name,
                    token = underlying,
                    poolType = "spark",
                    positionFetcher = PositionFetcher(
                        aToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
                    ),
                    marketToken = aToken,
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

    override fun getProtocol() = Protocol.SPARK

    override fun getNetwork() = Network.ETHEREUM
}