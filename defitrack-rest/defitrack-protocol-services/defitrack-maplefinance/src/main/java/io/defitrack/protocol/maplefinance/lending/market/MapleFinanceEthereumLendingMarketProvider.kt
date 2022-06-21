package io.defitrack.protocol.maplefinance.lending.market

import io.defitrack.common.network.Network
import io.defitrack.market.lending.LendingMarketService
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.maplefinance.MapleFinanceEthereumGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.stereotype.Component

@Component
class MapleFinanceEthereumLendingMarketProvider(
    private val erc20Resource: ERC20Resource,
    private val mapleFinanceEthereumGraphProvider: MapleFinanceEthereumGraphProvider,
    private val priceResource: PriceResource
) : LendingMarketService() {

    override suspend fun fetchLendingMarkets(): List<LendingMarket> = coroutineScope {
        mapleFinanceEthereumGraphProvider.getLendingMarkets().map {
            async {
                try {
                    val token = erc20Resource.getTokenInformation(getNetwork(), it.id)

                    LendingMarket(
                        id = "maplefinance-ethereum-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = it.name,
                        rate = it.rates.firstOrNull()?.rate?.toBigDecimal(),
                        poolType = "maplefinance-lending",
                        token = token.toFungibleToken()
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol() = Protocol.MAPLEFINANCE

    override fun getNetwork() = Network.ETHEREUM
}