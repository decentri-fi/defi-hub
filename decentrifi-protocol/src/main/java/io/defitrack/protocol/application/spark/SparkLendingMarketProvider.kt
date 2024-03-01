package io.defitrack.protocol.application.spark

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.spark.PoolContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SPARK)
class SparkLendingMarketProvider : LendingMarketProvider() {

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<LendingMarket> {

        val poolContract = PoolContract(
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
                    positionFetcher = defaultPositionFetcher(aToken.address),
                    marketToken = aToken,
                    totalSupply = refreshable {
                        getToken(aToken.address).totalDecimalSupply()
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