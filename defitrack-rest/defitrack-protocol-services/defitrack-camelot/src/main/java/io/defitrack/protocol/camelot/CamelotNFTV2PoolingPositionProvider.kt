package io.defitrack.protocol.camelot

import io.defitrack.market.pooling.PoolingPositionProvider
import io.defitrack.market.pooling.domain.PoolingPosition
import org.springframework.stereotype.Component

@Component
class CamelotNFTV2PoolingPositionProvider(
    private val camelotNFTV2PoolingMarketProvider: CamelotNFTV2PoolingMarketProvider
) : PoolingPositionProvider() {

    private val CAMELOT_NFT = "0xacdcc3c6a2339d08e0ac9f694e4de7c52f890db3"

    val algebraPositionsContract by lazy {
        AlgebraPositionsV2Contract(
            blockchainGateway = camelotNFTV2PoolingMarketProvider.getBlockchainGateway(),
            address = CAMELOT_NFT
        )
    }

    override suspend fun fetchUserPoolings(address: String): List<PoolingPosition> {
        val positions = algebraPositionsContract.getUserPositions(address)
        return positions.map {
            PoolingPosition(
                it.liquidity,
                camelotNFTV2PoolingMarketProvider.toMarket(it)
            )
        }
    }
}