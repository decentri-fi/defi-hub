package io.defitrack.protocol.mstable.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.BalanceFetcher
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.contract.MStableEthereumSavingsContract
import io.defitrack.protocol.mstable.MStablePolygonService
import io.defitrack.token.ERC20Resource

@Deprecated("not a lending market")
class MStablePolygonLendingMarketProvider(
    private val mStableService: MStablePolygonService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : LendingMarketProvider() {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    override suspend fun fetchMarkets(): List<LendingMarket> {
        return mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                savingsContractABI,
                it
            )
        }.map {
            val token = tokenService.getTokenInformation(getNetwork(), it.underlying())
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