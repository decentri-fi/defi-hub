package io.defitrack.protocol.mstable.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumService
import io.defitrack.protocol.mstable.contract.MStableEthereumSavingsContract
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

@Deprecated("not a lending market")
class MStableEthereumLendingMarketProvider(
    private val mStableService: MStableEthereumService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
) : LendingMarketProvider() {

    val savingsContractABI by lazy {
        runBlocking {
            abiResource.getABI("mStable/SavingsContract.json")
        }
    }

    override suspend fun fetchMarkets(): List<LendingMarket> = coroutineScope {
        mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                getBlockchainGateway(),
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

    private suspend fun toLendingMarket(it: MStableEthereumSavingsContract): LendingMarket {
        val token = tokenService.getTokenInformation(getNetwork(), it.underlying())
        return create(
            identifier = it.address,
            name = token.name,
            token = token.toFungibleToken(),
            poolType = "mstable",
            positionFetcher = PositionFetcher(
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