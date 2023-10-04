package io.defitrack.protocol.camelot

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.algebra.AlgebraFactoryContract
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CamelotService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val CAMELOT_NFT = "0xacdcc3c6a2339d08e0ac9f694e4de7c52f890db3"
    private val CAMELOT_FACTORY = "0xd490f2f6990c0291597fd1247651b4e0dcf684dd"

    val logger = LoggerFactory.getLogger(this::class.java)

    val algebraFactoryContract = AsyncUtils.lazyAsync {
        AlgebraFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            CAMELOT_FACTORY
        )
    }

    val algebraPositionsContract = AsyncUtils.lazyAsync {
        io.defitrack.protocol.algebra.AlgebraPositionsV2Contract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            address = CAMELOT_NFT
        )
    }

    suspend fun getPoolByPair(token0: String, token1: String) =
        algebraFactoryContract.await().getPoolByPair(token0, token1)

    suspend fun getAllPositions(): List<io.defitrack.protocol.algebra.AlgebraPosition> {
        return algebraPositionsContract.await().getAllPositions()
    }
}