package io.defitrack.protocol.application.aave.v2.lending.market

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.LazyValue
import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import io.defitrack.protocol.aave.v2.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.v2.contract.LendingPoolContract
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.application.aave.v2.lending.invest.AaveV2LendingInvestmentPreparer
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

    val lendingPoolContract = LazyValue {
        LendingPoolContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            lendingPoolAddressesProviderContract.await().lendingPoolAddress()
        )
    }

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        aaveV2PolygonService.getReserves()
            .filter {
                it.totalLiquidity > BigInteger.ZERO
            }.parMapNotNull(concurrency = 12) { reserve ->
                createMarket(reserve)
            }
    }

    private suspend fun createMarket(
        reserve: AaveReserve
    ) = catch {
        val aToken = getToken(reserve.aToken.id)
        val token = getToken(reserve.underlyingAsset)

        create(
            identifier = reserve.id,
            token = token,
            name = "aave v2 " + reserve.name,
            rate = reserve.lendingRate.toBigDecimal(),
            marketSize = calculateMarketSize(reserve, aToken, token),
            poolType = "aave.v2.lending",
            investmentPreparer = AaveV2LendingInvestmentPreparer(
                token.address,
                lendingPoolContract.get(),
                erC20Resource,
                balanceResource
            ),
            positionFetcher = PositionFetcher(
                aToken.asERC20Contract(getBlockchainGateway())::balanceOfFunction
            ),
            marketToken = aToken,
            totalSupply = refreshable(aToken.totalSupply.asEth(aToken.decimals)) {
                getToken(reserve.aToken.id).totalSupply.asEth(aToken.decimals)
            }
        )
    }.mapLeft {
        logger.error("uanble to get market: {}", reserve.id)
    }.getOrNull()

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