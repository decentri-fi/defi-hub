package io.defitrack.protocol.aave.v2.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
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
import org.springframework.stereotype.Service
import java.math.BigDecimal

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

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return aaveV2MainnetService.getReserves().map {
            val token = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            LendingMarket(
                id = "ethereum-aave-${it.symbol}",
                address = it.underlyingAsset,
                token = token.toFungibleToken(),
                name = it.name + " Aave Pool",
                protocol = getProtocol(),
                network = getNetwork(),
                poolType = "aave-v2",
                marketSize = calculateMarketSize(it).toBigDecimal(),
                investmentPreparer = AaveLendingInvestmentPreparer(
                    token.address,
                    lendingPoolContract,
                    erC20Resource
                )
            )
        }
    }

    private suspend fun calculateMarketSize(reserve: AaveReserve): Double {
        val underlying = erC20Resource.getTokenInformation(getNetwork(), reserve.underlyingAsset)
        return priceResource.calculatePrice(
            PriceRequest(
                underlying.address,
                getNetwork(),
                reserve.totalLiquidity.toBigDecimal().divide(BigDecimal.TEN.pow(reserve.decimals)),
                underlying.type
            )
        )
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM
}