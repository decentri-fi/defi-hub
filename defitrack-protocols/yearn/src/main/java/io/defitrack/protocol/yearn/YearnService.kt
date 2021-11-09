package io.defitrack.protocol.yearn

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class YearnService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun provideYearnV2Vaults(): List<YearnV2Vault> =
        runBlocking {
            val query = """
        {
            vaults {
            id
          }
        }
        """.trimIndent()

            val response = query(query)
            val poolSharesAsString =
                JsonParser.parseString(response).asJsonObject["data"].asJsonObject["vaults"].toString()
            return@runBlocking objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<YearnV2Vault>>() {

                })
        }

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/jainkunal/yearnvaultsv2subgraph") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}