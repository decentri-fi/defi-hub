package io.defitrack.protocol.yearn

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.thegraph.TheGraphGatewayProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class YearnService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider,
) {

    val graph =
        graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/jainkunal/yearnvaultsv2subgraph")

    suspend fun provideYearnV2Vaults(): List<YearnV2Vault> =
        runBlocking {
            val query = """
             {
            vaults {
              id
              token {
                id
                name
                symbol
                decimals
              }
              shareToken {
                id
                name
                symbol
                decimals
              }
              apiVersion
            } 
            }
        """.trimIndent()

            val poolSharesAsString = graph.performQuery(query).asJsonObject["vaults"].toString()
            return@runBlocking objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<YearnV2Vault>>() {

                })
        }
}