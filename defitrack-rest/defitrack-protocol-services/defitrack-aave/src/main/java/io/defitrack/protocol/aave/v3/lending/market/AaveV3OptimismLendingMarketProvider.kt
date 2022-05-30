package io.defitrack.protocol.aave.v3.lending.market

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v3.AaveV3OptimismDataProvider
import io.defitrack.protocol.aave.v3.contract.PoolContract
import io.defitrack.protocol.aave.v3.contract.PoolDataProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class AaveV3OptimismLendingMarketProvider(
    contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val aaveV3OptimismDataProvider: AaveV3OptimismDataProvider
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

            //   val reserveData = poolDataProvider.getReserveData(it)
            val reserveTokenAddresses = poolDataProvider.getReserveTokensAddresses(it)
            val aToken = erC20Resource.getTokenInformation(getNetwork(), reserveTokenAddresses.aTokenAddress)
            val underlying = erC20Resource.getTokenInformation(getNetwork(), it)

            LendingMarket(
                id = "aave-v3-optimism-$it",
                name = aToken.name,
                network = getNetwork(),
                protocol = getProtocol(),
                address = aToken.address,
                token = underlying.toFungibleToken(),
                poolType = "aave-v3",
            )
        }
    }

    override fun getProtocol() = Protocol.AAVE

    override fun getNetwork() = Network.OPTIMISM
}