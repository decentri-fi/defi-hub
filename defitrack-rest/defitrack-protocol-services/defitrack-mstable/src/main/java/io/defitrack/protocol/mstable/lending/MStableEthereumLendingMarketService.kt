package io.defitrack.protocol.mstable.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.BalanceFetcher
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumSavingsContract
import io.defitrack.protocol.mstable.MStableEthereumService
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Deprecated("not a lending market")
class MStableEthereumLendingMarketService(
    private val mStableService: MStableEthereumService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : LendingMarketService() {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    override suspend fun fetchLendingMarkets(): List<LendingMarketElement> = coroutineScope {
        mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                contractAccessorGateway.getGateway(getNetwork()),
                savingsContractABI,
                it
            )
        }.map {
            async {
                try {
                    toLendingMarket(it)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun toLendingMarket(it: MStableEthereumSavingsContract): LendingMarketElement {
        val token = tokenService.getTokenInformation(getNetwork(), it.underlying)
        return LendingMarketElement(
            id = "mstable-polygon-${it.address}",
            network = getNetwork(),
            protocol = getProtocol(),
            address = it.address,
            name = token.name,
            token = token.toFungibleToken(),
            marketSize = 0.0,
            rate = 0.0,
            poolType = "mstable",
            balanceFetcher = BalanceFetcher(
                address = it.address,
                { user -> it.balanceOfMethod(user) }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}