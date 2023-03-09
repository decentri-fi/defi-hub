package io.defitrack.protocol.balancer.graph

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.protocol.balancer.Pool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import kotlin.system.measureTimeMillis

@Component
class BalancerEthereumPoolGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-v2",
    theGraphGatewayProvider
), BalancerPoolGraphProvider {

    val cache = Cache.Builder().build<String, Pool>()
    val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() = runBlocking {
        logger.info("Initializing BalancerEthereumPoolGraphProvider")
        val millis = measureTimeMillis {
            getPools().forEach { pool ->
                cache.put(pool.address, pool)
            }
        }
        logger.info("Initialized BalancerEthereumPoolGraphProvider in ${millis / 1000} s")
    }

    suspend fun getPools(): List<Pool> {
        val query = """
            {
               pools(first: 1000, orderBy: totalLiquidity, orderDirection: desc) {
                address
                id
                totalLiquidity,
                totalShares
                tokens {
                    id
                    address
                    symbol
                    name
                    decimals
                    balance
                }
                symbol
                name
              }
            }
        """.trimIndent()

        return query(query, "pools")
    }

    override suspend fun getPool(poolAddress: String): Pool? {
        return cache.get(poolAddress)
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}