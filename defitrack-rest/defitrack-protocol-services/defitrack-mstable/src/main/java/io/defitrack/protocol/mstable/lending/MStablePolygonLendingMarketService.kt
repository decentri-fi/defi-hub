package io.defitrack.protocol.mstable.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.BalanceFetcher
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumSavingsContract
import io.defitrack.protocol.mstable.MStablePolygonService
import io.defitrack.token.ERC20Resource

@Deprecated("not a lending market")
class MStablePolygonLendingMarketService(
    private val mStableService: MStablePolygonService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : LendingMarketService() {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    override suspend fun fetchLendingMarkets(): List<LendingMarket> {
        return mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                contractAccessorGateway.getGateway(getNetwork()),
                savingsContractABI,
                it
            )
        }.map {
            val token = tokenService.getTokenInformation(getNetwork(), it.underlying)
            LendingMarket(
                id = "mstable-polygon-${it.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.address,
                name = token.name,
                token = token.toFungibleToken(),
                poolType = "mstable",
                balanceFetcher = BalanceFetcher(
                    address = it.address,
                    { user -> it.balanceOfMethod(user) }
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}