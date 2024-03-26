package io.defitrack.protocol.application.seamless

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.protocol.application.aave.v3.lending.invest.AaveV3LendingInvestmentPreparer
import io.defitrack.protocol.seamless.SeamlessAaveV3DataProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SEAMLESS)
class SeamlessLendingMarketProvider(
    private val dataProvider: SeamlessAaveV3DataProvider
) : LendingMarketProvider() {

    override suspend fun fetchMarkets(): List<LendingMarket> {

        val poolDataProvider = PoolDataProvider(
            getBlockchainGateway(),
            dataProvider.configs[getNetwork()]!!.poolDataProvider
        )

        val poolContract = PoolContract(
            getBlockchainGateway(),
            dataProvider.configs[getNetwork()]!!.poolAddress
        )

        return poolContract
            .reservesList()
            .parMapNotNull(concurrency = 8) { reserve ->
                catch {
                    createMarket(poolDataProvider, reserve, poolContract)
                }.mapLeft {
                    logger.error("Error while fetching aave v3 market $it", it)
                }.getOrNull()
            }
    }

    private suspend fun createMarket(
        poolDataProvider: PoolDataProvider,
        it: String,
        poolContract: PoolContract
    ): LendingMarket {
        val reserveData = poolDataProvider.getReserveData(it)
        val reserveTokenAddresses = poolDataProvider.getReserveTokensAddresses(it)
        val aToken = getToken(reserveTokenAddresses.aTokenAddress)
        val underlying = getToken(it)

        return create(
            identifier = aToken.address,
            name = aToken.name,
            token = underlying,
            poolType = "seamless",
            rate = reserveData.liquidityRate.asEth(27),
            investmentPreparer = AaveV3LendingInvestmentPreparer(
                underlying.address,
                poolContract,
                getERC20Resource(),
                balanceResource
            ),
            marketSize = refreshable {
                getPrice(
                    underlying.address,
                    poolDataProvider.getATokenTotalSupply(it).asEth(aToken.decimals)
                )
            },
            positionFetcher = defaultPositionFetcher(aToken.address),
            marketToken = aToken,
            totalSupply = refreshable {
                getToken(aToken.address).totalSupply.asEth(aToken.decimals)
            }
        )
    }

    override fun getProtocol() = Protocol.SEAMLESS

    override fun getNetwork() = Network.BASE
}