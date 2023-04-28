package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.algebra.AlgebraFactoryContract
import io.defitrack.protocol.algebra.AlgebraPoolContract
import io.defitrack.protocol.algebra.AlgebraPosition
import io.defitrack.protocol.algebra.AlgebraPositionsV2Contract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.time.Duration.Companion.hours

private const val CAMELOT_NFT = "0xacdcc3c6a2339d08e0ac9f694e4de7c52f890db3"
private const val CAMELOT_FACTORY = "0xd490f2f6990c0291597fd1247651b4e0dcf684dd"


@Component
class CamelotService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    val algebraFactoryContract by lazy {
        AlgebraFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            CAMELOT_FACTORY
        )
    }

    val algebraPositionsContract by lazy {
        AlgebraPositionsV2Contract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            address = CAMELOT_NFT
        )
    }

    val cache = Cache.Builder().expireAfterAccess(1.hours).build<String, List<AlgebraPoolContract>>()

    @PostConstruct
    fun init() = runBlocking {
        logger.info("importing camelot markets")
        cache.put("all", getPools())
        logger.info("done importing camelot markets")
    }

    suspend fun getPools(): List<AlgebraPoolContract> = coroutineScope {
        cache.get("all") {
            val sema = Semaphore(16)
            getAllPositions().distinctBy {
                listOf(it.token0, it.token1).sorted().joinToString("-")
            }.map {
                async {
                    sema.withPermit {
                        algebraFactoryContract.getPoolByPair(it.token0, it.token1)
                    }
                }
            }.awaitAll().distinct().map {
                AlgebraPoolContract(
                    blockchainGatewayProvider.getGateway(Network.ARBITRUM),
                    it
                )
            }
        }
    }

    suspend fun getPoolByPair(token0: String, token1: String) = algebraFactoryContract.getPoolByPair(token0, token1)

    suspend fun getAllPositions(): List<AlgebraPosition> {
        return algebraPositionsContract.getAllPositions()
    }
}