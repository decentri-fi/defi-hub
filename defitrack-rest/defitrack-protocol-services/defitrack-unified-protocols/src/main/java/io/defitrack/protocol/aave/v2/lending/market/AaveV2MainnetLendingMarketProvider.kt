package io.defitrack.protocol.aave.v2.lending.market

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2MainnetService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.lending.invest.AaveV2LendingInvestmentPreparer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveV2MainnetLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    private val aaveV2MainnetService: AaveV2MainnetService,
    private val priceResource: PriceResource,
) : LendingMarketProvider() {

    val lendingPoolAddressesProviderContract = lazyAsync {
        LendingPoolAddressProviderContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            aaveV2MainnetService.getLendingPoolAddressesProvider()
        )
    }

    val lendingPoolContract = lazyAsync {
        LendingPoolContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            lendingPoolAddressesProviderContract.await().lendingPoolAddress()
        )
    }

    override suspend fun produceMarkets(): Flow<LendingMarket> = channelFlow {
        aaveV2MainnetService.getReserves()
            .filter {
                it.totalLiquidity > BigInteger.ZERO
            }
            .forEach {
                launch {
                    throttled {
                        try {
                            val aToken = getToken(it.aToken.id)
                            val token = getToken(it.underlyingAsset)
                            val market = create(
                                identifier = it.id,
                                token = token.toFungibleToken(),
                                name = "aave v2 " + it.name,
                                poolType = "aave-v2",
                                marketSize = calculateMarketSize(it, aToken, token),
                                investmentPreparer = AaveV2LendingInvestmentPreparer(
                                    token.address,
                                    lendingPoolContract.await(),
                                    getERC20Resource()
                                ),
                                rate = it.lendingRate.toBigDecimal(),
                                positionFetcher = PositionFetcher(
                                    aToken.address,
                                    { user ->
                                        balanceOfFunction(user)
                                    }
                                ),
                                marketToken = aToken.toFungibleToken(),
                                totalSupply = refreshable(aToken.totalSupply.asEth(aToken.decimals)) {
                                    getToken(it.aToken.id).totalSupply.asEth(aToken.decimals)
                                }
                            )

                            send(market)
                        } catch (ex: Exception) {
                            logger.error("Unable to fetch lending market with address $it", ex)
                        }
                    }
                }
            }
    }

    override fun getProtocol(): Protocol {
        return Protocol.AAVE_V2
    }

    private suspend fun calculateMarketSize(
        reserve: AaveReserve,
        aToken: TokenInformationVO,
        underlyingToken: TokenInformationVO
    ): Refreshable<BigDecimal> {
        return refreshable {
            val underlying = getToken(underlyingToken.address)
            priceResource.calculatePrice(
                PriceRequest(
                    underlying.address,
                    getNetwork(),
                    reserve.totalLiquidity.asEth(aToken.decimals),
                    underlying.type
                )
            ).toBigDecimal()
        }
    }

    override fun getNetwork(): Network = Network.ETHEREUM
}