package io.defitrack.protocol.aave.v2.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.lending.invest.AaveLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class AaveV2PolygonLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    private val aaveV2PolygonService: AaveV2PolygonService,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract = LendingPoolAddressProviderContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPoolAddressesProvider.json"),
        aaveV2PolygonService.getLendingPoolAddressesProvider()
    )

    val lendingPoolContract = LendingPoolContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPool.json"),
        lendingPoolAddressesProviderContract.lendingPoolAddress()
    )

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        aaveV2PolygonService.getReserves()
            .filter {
                it.totalLiquidity > BigInteger.ZERO
            }.map {
                async {
                    try {
                        val aToken = erC20Resource.getTokenInformation(getNetwork(), it.aToken.id)
                        val token = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
                        create(
                            identifier = it.id,
                            token = token.toFungibleToken(),
                            name = it.name + " Aave Pool",
                            rate = it.lendingRate.toBigDecimal(),
                            marketSize = calculateMarketSize(it, aToken, token).toBigDecimal(),
                            poolType = "aave-v2",
                            investmentPreparer = AaveLendingInvestmentPreparer(
                                token.address,
                                lendingPoolContract,
                                erC20Resource
                            ),
                        )
                    } catch (ex: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()
    }

    private suspend fun calculateMarketSize(reserve: AaveReserve, aToken: TokenInformation, underlyingToken: TokenInformation): Double {
        val underlying = erC20Resource.getTokenInformation(getNetwork(), underlyingToken.address)
        return priceResource.calculatePrice(
            PriceRequest(
                underlying.address,
                getNetwork(),
                reserve.totalLiquidity.asEth(aToken.decimals),
                underlying.type
            )
        )
    }


    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}