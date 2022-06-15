package io.defitrack.protocol.makerdao.lending.market

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingMarketService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.makerdao.MakerDAOEthereumGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.stereotype.Component

@Component
class MakerDAOEthereumLendingMarketProvider(
    private val erc20Resource: ERC20Resource,
    private val makerDAOEthereumGraphProvider: MakerDAOEthereumGraphProvider,
    private val priceResource: PriceResource
) : LendingMarketService() {

    override suspend fun fetchLendingMarkets(): List<LendingMarket> = coroutineScope {
        makerDAOEthereumGraphProvider.getLendingMarkets().map {
            async {
                try {
                    val token = erc20Resource.getTokenInformation(getNetwork(), it.id)

                    LendingMarket(
                        id = "makerdao-ethereum-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = it.name,
                        rate = it.rates[0].rate.toBigDecimal(),
                        poolType = "makerdao-lending",
                        token = token.toFungibleToken()
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol() = Protocol.MAKERDAO

    override fun getNetwork() = Network.ETHEREUM
}