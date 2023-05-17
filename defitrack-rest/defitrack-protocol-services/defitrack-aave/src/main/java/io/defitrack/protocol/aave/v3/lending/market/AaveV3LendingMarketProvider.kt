package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.common.utils.RefetchableValue
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.protocol.aave.v3.lending.invest.AaveV3LendingInvestmentPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

abstract class AaveV3LendingMarketProvider(
    private val network: Network,
    abiResource: ABIResource,
    aaveV3DataProvider: AaveV3DataProvider,
) : LendingMarketProvider() {

    val pool by lazy {
        runBlocking {
            PoolContract(
                getBlockchainGateway(),
                abiResource.getABI("aave/v3/Pool.json"),
                aaveV3DataProvider.poolAddress
            )
        }
    }

    val poolDataProvider by lazy {
        runBlocking {
            PoolDataProvider(
                getBlockchainGateway(),
                abiResource.getABI("aave/v3/AaveProtocolDataProvider.json"),
                aaveV3DataProvider.poolDataProvider
            )
        }
    }

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        pool.reservesList().map {
            async {
                try {

                    val reserveData = poolDataProvider.getReserveData(it)
                    val reserveTokenAddresses = poolDataProvider.getReserveTokensAddresses(it)
                    val aToken = getToken(reserveTokenAddresses.aTokenAddress)
                    val underlying = getToken(it)
                    val totalSupply = poolDataProvider.getATokenTotalSupply(it)

                    create(
                        identifier = aToken.address,
                        name = "aave v3 " + aToken.name,
                        token = underlying.toFungibleToken(),
                        poolType = "aave-v3",
                        rate = reserveData.liquidityRate.asEth(27),
                        investmentPreparer = AaveV3LendingInvestmentPreparer(
                            underlying.address,
                            pool,
                            getERC20Resource()
                        ),
                        marketSize = RefetchableValue.refetchable {
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
                            aToken.address,
                            { user -> balanceOfFunction(user) },
                        ),
                        marketToken = aToken.toFungibleToken(),
                        totalSupply = RefetchableValue.refetchable(aToken.totalSupply.asEth(aToken.decimals)) {
                            val aToken = getToken(aToken.address)
                            aToken.totalSupply.asEth(aToken.decimals)
                        }
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to fetch V3 Lending market with address $it", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol() = Protocol.AAVE_V3

    override fun getNetwork() = network
}