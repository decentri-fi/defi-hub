package io.defitrack.protocol.mstable

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarketElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class MStablePolygonLendingMarketService(
    private val mStableService: MStablePolygonService,
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val polygonContractAccessor: PolygonContractAccessor,
) : LendingMarketService() {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    override suspend fun fetchLendingMarkets(): List<LendingMarketElement> {
        return mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                polygonContractAccessor,
                savingsContractABI,
                it
            )
        }.map {
            val token = tokenService.getTokenInformation(getNetwork(), it.underlying)
            LendingMarketElement(
                id = "mstable-polygon-${it.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.address,
                name = token.name,
                token = token.toFungibleToken(),
                marketSize = 0.0,
                rate = 0.0,
                poolType = "mstable"
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