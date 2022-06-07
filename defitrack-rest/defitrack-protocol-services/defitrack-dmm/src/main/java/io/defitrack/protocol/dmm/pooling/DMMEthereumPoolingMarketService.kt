package io.defitrack.protocol.dmm.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dmm.DMMEthereumGraphProvider
import io.defitrack.protocol.dmm.apr.DMMAPRService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class DMMEthereumPoolingMarketService(
    private val dmmPolygonService: DMMEthereumGraphProvider,
    private val dmmaprService: DMMAPRService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> = coroutineScope {
        dmmPolygonService.getPoolingMarkets().map {
            async(Dispatchers.IO.limitedParallelism(10)) {
                try {
                    val token = erc20Resource.getTokenInformation(getNetwork(), it.id)
                    val token0 = erc20Resource.getTokenInformation(getNetwork(), it.token0.id)
                    val token1 = erc20Resource.getTokenInformation(getNetwork(), it.token1.id)

                    PoolingMarketElement(
                        id = "dmm-ethereum-${it.id}",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        address = it.id,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = listOf(
                            token0.toFungibleToken(),
                            token1.toFungibleToken()
                        ),
                        apr = dmmaprService.getAPR(it.pair.id, getNetwork()),
                        marketSize = it.reserveUSD,
                        tokenType = TokenType.DMM
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DMM
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}