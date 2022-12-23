package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3DataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.protocol.aave.v3.lending.invest.AaveV3LendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

abstract class AaveV3LendingMarketProvider(
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val network: Network,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    aaveV3DataProvider: AaveV3DataProvider,
) : LendingMarketProvider() {

    val pool = PoolContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/v3/Pool.json"),
        aaveV3DataProvider.poolAddress
    )

    val poolDataProvider = PoolDataProvider(
        blockchainGatewayProvider.getGateway(getNetwork()),
        abiResource.getABI("aave/v3/AaveProtocolDataProvider.json"),
        aaveV3DataProvider.poolDataProvider
    )

    override suspend fun fetchLendingMarkets(): List<LendingMarket> = coroutineScope {
        pool.reservesList.map {
            async {
                try {

                    val reserveData = poolDataProvider.getReserveData(it)
                    val reserveTokenAddresses = poolDataProvider.getReserveTokensAddresses(it)
                    val aToken = erC20Resource.getTokenInformation(getNetwork(), reserveTokenAddresses.aTokenAddress)
                    val underlying = erC20Resource.getTokenInformation(getNetwork(), it)
                    val totalSupply = poolDataProvider.getATokenTotalSupply(it)

                    LendingMarket(
                        id = "aave-v3-optimism-${aToken.address}",
                        name = aToken.name,
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = aToken.address,
                        token = underlying.toFungibleToken(),
                        poolType = "aave-v3",
                        rate = reserveData.liquidityRate.asEth(27),
                        investmentPreparer = AaveV3LendingInvestmentPreparer(
                            underlying.address,
                            pool,
                            erC20Resource
                        ),
                        marketSize = priceResource.calculatePrice(
                            PriceRequest(
                                underlying.address,
                                getNetwork(),
                                totalSupply.asEth(aToken.decimals),
                                underlying.type
                            )
                        ).toBigDecimal()
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to fetch V3 Lending market with address $it")
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol() = Protocol.AAVE

    override fun getNetwork() = network
}