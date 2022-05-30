package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3OptimismDataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class AaveV3OptimismLendingMarketProvider(
    contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val aaveV3OptimismDataProvider: AaveV3OptimismDataProvider,
    private val priceResource: PriceResource
) : LendingMarketService() {

    val pool = PoolContract(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("aave/v3/Pool.json"),
        aaveV3OptimismDataProvider.getPoolAddress()
    )

    val poolDataProvider = PoolDataProvider(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("aave/v3/AaveProtocolDataProvider.json"),
        aaveV3OptimismDataProvider.getPoolDataProvider()
    )

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return pool.reservesList.map {

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
                marketSize = priceResource.calculatePrice(
                    PriceRequest(
                        underlying.address,
                        getNetwork(),
                        totalSupply.asEth(aToken.decimals),
                        underlying.type
                    )
                ).toBigDecimal()
            )
        }
    }

    override fun getProtocol() = Protocol.AAVE

    override fun getNetwork() = Network.OPTIMISM
}