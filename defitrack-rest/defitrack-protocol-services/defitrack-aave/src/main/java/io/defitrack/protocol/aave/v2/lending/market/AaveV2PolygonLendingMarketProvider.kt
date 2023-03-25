package io.defitrack.protocol.aave.v2.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.lending.invest.AaveV2LendingInvestmentPreparer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AaveV2PolygonLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    private val aaveV2PolygonService: AaveV2PolygonService,
    private val priceResource: PriceResource
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract by lazy {
        runBlocking {
            LendingPoolAddressProviderContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                abiResource.getABI("aave/LendingPoolAddressesProvider.json"),
                aaveV2PolygonService.getLendingPoolAddressesProvider()
            )
        }
    }

    val lendingPoolContract by lazy {
       runBlocking {
           LendingPoolContract(
               blockchainGatewayProvider.getGateway(getNetwork()),
               abiResource.getABI("aave/LendingPool.json"),
               lendingPoolAddressesProviderContract.lendingPoolAddress()
           )
       }
    }

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
                            token = token.toFungibleToken(),
                            name = "aave v2" + it.name,
                            rate = it.lendingRate.toBigDecimal(),
                            marketSize = calculateMarketSize(it, aToken, token).toBigDecimal(),
                            poolType = "aave-v2",
                            investmentPreparer = AaveV2LendingInvestmentPreparer(
                                token.address,
                                lendingPoolContract,
                                getERC20Resource()
                            ),
                            positionFetcher = PositionFetcher(
                                aToken.address,
                                { user ->
                                    getERC20Resource().balanceOfFunction(aToken.address, user, getNetwork())
                                }
                            )
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
    }

    private suspend fun calculateMarketSize(
        reserve: AaveReserve,
        aToken: TokenInformationVO,
        underlyingToken: TokenInformationVO
    ): Double {
        val underlying = getToken(underlyingToken.address)
        return priceResource.calculatePrice(
            PriceRequest(
                underlying.address,
                getNetwork(),
                reserve.totalLiquidity.asEth(aToken.decimals),
                underlying.type
            )
        )
    }

    override fun getNetwork(): Network = Network.POLYGON
}