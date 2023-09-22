package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.hours

private const val CAMELOT_NFT = "0xacdcc3c6a2339d08e0ac9f694e4de7c52f890db3"
private const val CAMELOT_FACTORY = "0xd490f2f6990c0291597fd1247651b4e0dcf684dd"


@Component
class CamelotService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    val algebraFactoryContract = lazyAsync {
        AlgebraFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            CAMELOT_FACTORY
        )
    }

    val algebraPositionsContract = lazyAsync {
        AlgebraPositionsV2Contract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            address = CAMELOT_NFT
        )
    }

    val cache = Cache.Builder<String, List<AlgebraPoolContract>>().expireAfterAccess(1.hours).build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24) // 24 hours
    fun init() = runBlocking {
        logger.info("importing camelot markets")
        val pools = fetchPools()
        cache.put("all", pools)
        logger.info("done importing ${pools.size} camelot markets")
    }

    suspend fun getPools(): List<AlgebraPoolContract> = coroutineScope {
        cache.get("all") {
            fetchPools()
        }
    }

    private suspend fun fetchPools(): List<AlgebraPoolContract> = coroutineScope {
        val sema = Semaphore(16)
        getAllPositions().distinctBy {
            listOf(it.token0, it.token1).sorted().joinToString("-")
        }.map {
            async {
                sema.withPermit {
                    algebraFactoryContract.await().getPoolByPair(it.token0, it.token1)
                }
            }
        }.awaitAll().distinct().map {
            AlgebraPoolContract(
                blockchainGatewayProvider.getGateway(Network.ARBITRUM),
                it
            )
        }
    }

    suspend fun getPoolByPair(token0: String, token1: String) = algebraFactoryContract.await().getPoolByPair(token0, token1)

    suspend fun getAllPositions(): List<AlgebraPosition> {
        return algebraPositionsContract.await().getAllPositions()
    }
}