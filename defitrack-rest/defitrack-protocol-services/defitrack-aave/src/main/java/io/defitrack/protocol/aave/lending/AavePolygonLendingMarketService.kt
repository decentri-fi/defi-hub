package io.defitrack.protocol.aave.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveV2PolygonService
import io.defitrack.protocol.aave.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.contract.LendingPoolContract
import io.defitrack.protocol.aave.domain.AaveReserve
import io.defitrack.protocol.aave.lending.invest.AaveLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AavePolygonLendingMarketService(
    contractAccessorGateway: ContractAccessorGateway,
    abiResource: ABIResource,
    private val aaveV2PolygonService: AaveV2PolygonService,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource
) : LendingMarketService() {

    val lendingPoolAddressesProviderContract = LendingPoolAddressProviderContract(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPoolAddressesProvider.json"),
        aaveV2PolygonService.getLendingPoolAddressesProvider()
    )

    val lendingPoolContract = LendingPoolContract(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPool.json"),
        lendingPoolAddressesProviderContract.lendingPoolAddress()
    )

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return aaveV2PolygonService.getReserves().map {
            val token = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            LendingMarket(
                id = "polygon-aave-${it.symbol}",
                address = it.underlyingAsset,
                token = token.toFungibleToken(),
                name = it.name + " Aave Pool",
                protocol = getProtocol(),
                network = getNetwork(),
                rate = it.lendingRate.toBigDecimal(),
                marketSize = calculateMarketSize(it).toBigDecimal(),
                poolType = "aave-v2",
                investmentPreparer = AaveLendingInvestmentPreparer(
                    token.address,
                    lendingPoolContract,
                    erC20Resource
                )
            )
        }
    }

    private suspend fun calculateMarketSize(reserve: AaveReserve): Double {
        return priceResource.calculatePrice(
            PriceRequest(
                reserve.underlyingAsset,
                getNetwork(),
                reserve.totalLiquidity.toBigDecimal().divide(BigDecimal.TEN.pow(reserve.decimals)),
                TokenType.SINGLE
            )
        )
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}