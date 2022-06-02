package io.defitrack.protocol.makerdao.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.v3.MakerDAODataProvider
import io.defitrack.protocol.makerdao.v3.contract.PoolContract
import io.defitrack.protocol.makerdao.v3.contract.PoolDataProvider
import io.defitrack.protocol.makerdao.v3.lending.invest.MakerDAOLendingInvestmentPreparer
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class MakerDAOLendingMarketProvider(
    contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val makerDAODataProvider: MakerDAODataProvider,
    private val priceResource: PriceResource
) : LendingMarketService() {

    val pool = PoolContract(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("makerdao/v3/Pool.json"),
        makerDAODataProvider.getPoolAddress()
    )

    val poolDataProvider = PoolDataProvider(
        contractAccessorGateway.getGateway(getNetwork()),
        abiResource.getABI("makerdao/v3/makerdaoProtocolDataProvider.json"),
        makerDAODataProvider.getPoolDataProvider()
    )

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return pool.reservesList.map {

            val reserveData = poolDataProvider.getReserveData(it)
            val reserveTokenAddresses = poolDataProvider.getReserveTokensAddresses(it)
            val aToken = erC20Resource.getTokenInformation(getNetwork(), reserveTokenAddresses.aTokenAddress)
            val underlying = erC20Resource.getTokenInformation(getNetwork(), it)
            val totalSupply = poolDataProvider.getATokenTotalSupply(it)

            LendingMarket(
                id = "makerdao-v3-optimism-${aToken.address}",
                name = aToken.name,
                network = getNetwork(),
                protocol = getProtocol(),
                address = aToken.address,
                token = underlying.toFungibleToken(),
                poolType = "makerdao-v3",
                rate = reserveData.liquidityRate.asEth(27),
                investmentPreparer = MakerDAOLendingInvestmentPreparer(
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
        }
    }

    override fun getProtocol() = Protocol.MAKERDAO

    override fun getNetwork() = Network.ETHEREUM
}