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
import io.defitrack.protocol.aave.v2.AaveV2MainnetService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.lending.invest.AaveLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AaveV2MainnetLendingMarketProvider(
    abiResource: ABIResource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val aaveV2MainnetService: AaveV2MainnetService,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract = LendingPoolAddressProviderContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPoolAddressesProvider.json"),
        aaveV2MainnetService.getLendingPoolAddressesProvider()
    )

    val lendingPoolContract = LendingPoolContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPool.json"),
        lendingPoolAddressesProviderContract.lendingPoolAddress()
    )

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        aaveV2MainnetService.getReserves()
            .filter {
                it.totalLiquidity > BigInteger.ZERO
            }
            .map {
                async {
                    try {
                        val aToken = erC20Resource.getTokenInformation(getNetwork(), it.aToken.id)
                        val token = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
                        create(
                            identifier = it.id,
                            token = token.toFungibleToken(),
                            name = it.name + " Aave Pool",
                            poolType = "aave-v2",
                            marketSize = calculateMarketSize(it, aToken, token).toBigDecimal(),
                            investmentPreparer = AaveLendingInvestmentPreparer(
                                token.address,
                                lendingPoolContract,
                                erC20Resource
                            ),
                            rate = it.lendingRate.toBigDecimal()
                        )
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch lending market with address $it", ex)
                        null
                    }
                }
            }.awaitAll().filterNotNull()
    }

    private suspend fun calculateMarketSize(
        reserve: AaveReserve,
        aToken: TokenInformation,
        underlyingToken: TokenInformation
    ): Double {
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

    override fun getNetwork(): Network = Network.ETHEREUM
}