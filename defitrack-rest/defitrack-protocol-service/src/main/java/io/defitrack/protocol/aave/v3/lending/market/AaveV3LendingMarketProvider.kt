package io.defitrack.protocol.aave.v3.lending.market

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.protocol.aave.v3.lending.invest.AaveV3LendingInvestmentPreparer

abstract class AaveV3LendingMarketProvider(
    private val network: Network,
    private val dataProvider: AaveV3DataProvider,
) : LendingMarketProvider() {


    override suspend fun fetchMarkets(): List<LendingMarket> {

        val poolDataProvider = PoolDataProvider(
            getBlockchainGateway(),
            dataProvider.poolDataProvider()
        )

        val poolContract = PoolContract(
            getBlockchainGateway(),
            dataProvider.poolAddress()
        )


        return poolContract
            .reservesList()
            .parMapNotNull(concurrency = 8) {
                catch {
                    createMarket(poolDataProvider, it, poolContract)
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
        val totalSupply = poolDataProvider.getATokenTotalSupply(it)

        return create(
            identifier = aToken.address,
            name = "aave v3 " + aToken.name,
            token = underlying,
            poolType = "aave-v3",
            rate = reserveData.liquidityRate.asEth(27),
            investmentPreparer = AaveV3LendingInvestmentPreparer(
                underlying.address,
                poolContract,
                getERC20Resource()
            ),
            marketSize = refreshable {
                getPriceResource().calculatePrice(
                    PriceRequest(
                        underlying.address,
                        getNetwork(),
                        totalSupply.asEth(aToken.decimals),
                        underlying.type
                    )
                ).toBigDecimal()
            },
            positionFetcher = PositionFetcher(
                aToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
            ),
            marketToken = aToken,
            totalSupply = refreshable(aToken.totalSupply.asEth(aToken.decimals)) {
                getToken(aToken.address).totalSupply.asEth(aToken.decimals)
            }
        )
    }

    override fun getProtocol() = Protocol.AAVE_V3

    override fun getNetwork() = network
}