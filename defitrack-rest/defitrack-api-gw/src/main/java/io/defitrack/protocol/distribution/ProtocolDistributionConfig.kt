package io.defitrack.protocol.distribution

import io.defitrack.company.CompanyVO
import io.defitrack.node.Node
import io.defitrack.protocol.Company
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class ProtocolDistributionConfig(
    @Value("\${decentrifi.group-nodes}") val groupNodes: List<String>,
    private val httpClient: HttpClient
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getConfigs(): List<Node> {
        return groupNodes.map {
            Node(
                name = it,
                companies = getActivatedCompanies(it).map { company ->
                    Company.findBySlug(company.slug)
                        ?: throw IllegalArgumentException("Unknown company ${company.slug}")
                }.distinct()
            ).also {
                logger.info("Loaded ${it.companies.size} companies for node $it")
            }
        }.also { nodes ->
            logger.info("Loaded ${nodes.size} nodes")
        }
    }

    suspend fun getActivatedCompanies(node: String): List<CompanyVO> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("http://defitrack-group-$node.default.svc.cluster.local:8080/")
            if (response.status.isSuccess()) {
                response.body()
            } else {
                logger.error("Unable to populate companies for node $node")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Unable to populate companies for node $node", e)
            emptyList()
        }
    }
}