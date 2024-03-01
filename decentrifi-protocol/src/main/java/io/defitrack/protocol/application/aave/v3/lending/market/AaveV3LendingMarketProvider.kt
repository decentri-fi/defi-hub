package io.defitrack.protocol.application.aave.v3.lending.market

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.protocol.application.aave.v3.lending.invest.AaveV3LendingInvestmentPreparer

abstract class AaveV3LendingMarketProvider(
    private val network: Network,
    private val dataProvider: AaveV3DataProvider,
) : LendingMarketProvider() {

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<LendingMarket> {

        val poolDataProvider = PoolDataProvider(
            dataProvider.poolDataProvider()
        )

        val poolContract = PoolContract(
            dataProvider.poolAddress()
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
            name = "aave v3 " + aToken.name,
            token = underlying,
            poolType = "aave.v3.lending",
            rate = reserveData.liquidityRate.asEth(27),
            investmentPreparer = AaveV3LendingInvestmentPreparer(
                underlying.address,
                poolContract,
                getERC20Resource()
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

    override fun getProtocol() = Protocol.AAVE_V3

    override fun getNetwork() = network
}