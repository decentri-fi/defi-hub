package io.defitrack.protocol.aave.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveV2MainnetService
import io.defitrack.protocol.aave.contract.LendingPoolAddressProviderContract
import io.defitrack.protocol.aave.contract.LendingPoolContract
import io.defitrack.protocol.aave.domain.AaveReserve
import io.defitrack.protocol.aave.lending.invest.AaveLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AaveV2MainnetLendingMarketService(
    abiResource: ABIResource,
    contractAccessorGateway: ContractAccessorGateway,
    private val aaveV2MainnetService: AaveV2MainnetService,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
) : LendingMarketService() {

    val lendingPoolAddressesProviderContract = LendingPoolAddressProviderContract(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("aave/LendingPoolAddressesProvider.json"),
        aaveV2MainnetService.getLendingPoolAddressesProvider()
    )

    val lendingPoolContract = LendingPoolContract(
        contractAccessorGateway.getGateway(getNetwork()),
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

    private fun calculateMarketSize(reserve: AaveReserve): Double {
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

    override fun getNetwork(): Network = Network.ETHEREUM
}