package io.defitrack.protocol.application.aave.v2.lending.market

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.application.aave.v2.lending.invest.AaveV2LendingInvestmentPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV2PolygonLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val aaveV2PolygonService: AaveV2PolygonService,
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract = lazyAsync {
        LendingPoolAddressProviderContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            aaveV2PolygonService.getLendingPoolAddressesProvider()
        )
    }

    val lendingPoolContract = lazyAsync {
        LendingPoolContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            lendingPoolAddressesProviderContract.await().lendingPoolAddress()
        )
    }

    //todo parmap
    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        aaveV2PolygonService.getReserves()
            .filter {
                it.totalLiquidity > BigInteger.ZERO
            }.map {
                async {
                    try {
                        val aToken = getToken(it.aToken.id)
                        val token = getToken(it.underlyingAsset)

                        create(
                            identifier = it.id,
                            token = token,
                            name = "aave v2" + it.name,
                            rate = it.lendingRate.toBigDecimal(),
                            marketSize = calculateMarketSize(it, aToken, token),
                            poolType = "aave-v2",
                            investmentPreparer = AaveV2LendingInvestmentPreparer(
                                token.address,
                                lendingPoolContract.await(),
                                getERC20Resource()
                            ),
                            positionFetcher = PositionFetcher(
                                aToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
                            ),
                            marketToken = aToken,
                            totalSupply = refreshable(aToken.totalSupply.asEth(aToken.decimals)) {
                                getToken(it.aToken.id).totalSupply.asEth(aToken.decimals)
                            }
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V2
    }

    private suspend fun calculateMarketSize(
        reserve: AaveReserve,
        aToken: FungibleTokenInformation,
        underlyingToken: FungibleTokenInformation
    ): Refreshable<BigDecimal> {
        val underlying = getToken(underlyingToken.address)
        return refreshable {
            getPriceResource().calculatePrice(
                GetPriceCommand(
                    underlying.address,
                    getNetwork(),
                    reserve.totalLiquidity.asEth(aToken.decimals),
                    underlying.type
                )
            ).toBigDecimal()
        }
    }

    override fun getNetwork(): Network = Network.POLYGON
}