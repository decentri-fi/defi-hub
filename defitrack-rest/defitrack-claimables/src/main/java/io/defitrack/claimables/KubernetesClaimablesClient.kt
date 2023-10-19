package io.defitrack.claimables

import arrow.fx.coroutines.parMap
import io.defitrack.claimable.vo.ClaimableMarketVO
import io.defitrack.claimable.vo.UserClaimableVO
import io.defitrack.node.Node
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolVO
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

@Component
@Profile("kubernetes")
class KubernetesClaimablesClient(
    private val httpClient: HttpClient
) : ClaimablesClient {

    val baseUrl = "https://api.decentri.fi"
    private val logger = LoggerFactory.getLogger(this::class.java)

    private lateinit var nodes: List<Node>

    init {
        suspend fun getNodes(): List<Node> {
            val response = httpClient.get("https://api.decentri.fi/nodes")
            return if (response.status.isSuccess()) {
                return response.body()
            } else emptyList()
        }

        runBlocking {
            nodes = getNodes()
        }
    }


    override suspend fun getClaimables(address: String, protocols: List<Protocol>): List<UserClaimableVO> =
        withContext(Dispatchers.IO) {

            val requiredCompanies = protocols.map(Protocol::company)
            val requiredNodes = nodes.filter { node ->
                node.companies.any {
                    requiredCompanies.contains(it)
                }
            }

            requiredNodes.parMap(EmptyCoroutineContext, 12) { node ->
                val timedValue: TimedValue<List<UserClaimableVO>> = measureTimedValue {
                    try {
                        val response =
                            httpClient.get("http://defitrack-group-${node}.default.svc.cluster.local:8080/claimables/$address")
                        if (response.status.isSuccess()) {
                            response.body()
                        } else {
                            emptyList()
                        }
                    } catch (ex: Exception) {
                        logger.error(
                            "Error getting claimables for $address on node ${node.name}",
                            ex
                        )
                        emptyList()
                    }
                }

                logger.info("took ${timedValue.duration} to get claimables for $address on node ${node.name}")
                timedValue.value
            }.flatten().filter {
                protocols.map(Protocol::slug).contains(it.protocol.slug)
            }
        }

    val cache = Cache.Builder<String, List<ClaimableMarketVO>>().expireAfterWrite(1.hours).build()
    override suspend fun getClaimableMarkets(protocol: ProtocolVO): List<ClaimableMarketVO> {
        return cache.get(protocol.slug) {
            withContext(Dispatchers.IO) {
                try {
                    val response = httpClient.get("$baseUrl/${protocol.slug}/claimables")
                    if (response.status.isSuccess()) {
                        response.body()
                    } else {
                        emptyList()
                    }
                } catch (ex: Exception) {
                    logger.error("Error getting claimables markets on ${protocol.company.slug}/${protocol.slug}", ex)
                    emptyList()
                }
            }
        }
    }
}