package io.codechef.defitrack.protocol.mstable

import io.codechef.common.network.Network
import io.codechef.defitrack.abi.ABIResource
import io.codechef.defitrack.lending.LendingMarketService
import io.codechef.defitrack.lending.domain.LendingMarketElement
import io.codechef.defitrack.lending.domain.LendingToken
import io.codechef.defitrack.token.TokenService
import io.codechef.ethereum.config.EthereumContractAccessor
import io.codechef.matic.config.PolygonContractAccessor
import io.codechef.mstable.MStablePolygonService
import io.codechef.protocol.Protocol
import okhttp3.internal.toImmutableList
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class MStablePolygonLendingMarketService(
    private val mStableService: MStablePolygonService,
    private val abiResource: ABIResource,
    private val tokenService: TokenService,
    private val polygonContractAccessor: PolygonContractAccessor,
) : LendingMarketService {

    val savingsContractABI by lazy {
        abiResource.getABI("mStable/SavingsContract.json")
    }

    val marketBuffer = mutableListOf<LendingMarketElement>()

    @PostConstruct
    fun init() {
        mStableService.getSavingsContracts().map {
            MStableEthereumSavingsContract(
                polygonContractAccessor,
                savingsContractABI,
                it
            )
        }.forEach {
            val token = tokenService.getTokenInformation(it.underlying, getNetwork())
            LendingMarketElement(
                id = "mstable-polygon-${it.address}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.address,
                name = token.name,
                token = LendingToken(
                    name = token.name,
                    symbol = token.symbol,
                    address = token.address
                ),
                marketSize = 0.0,
                rate = 0.0,
                poolType = "mstable"
            )
        }
    }

    override fun getLendingMarkets(): List<LendingMarketElement> {
        return marketBuffer.toImmutableList()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}