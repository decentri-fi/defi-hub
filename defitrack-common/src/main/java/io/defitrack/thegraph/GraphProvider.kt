package io.defitrack.thegraph

import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.JsonElement


abstract class GraphProvider(
    url: String,
    graphGatewayProvider: TheGraphGatewayProvider
) {
    val graph: TheGraphGateway = graphGatewayProvider.createTheGraphGateway(url)

    suspend inline fun <reified T> query(query: String, subPath: String): T {
        val result = graph.performQuery(query)
        return map(getSub(subPath, result))
    }

    fun getSub(subPath: String, json: JsonElement): JsonElement {
        val tokens = subPath.split("/")
        return if (tokens.size > 1) {
            getSub(tokens.takeLast(tokens.size - 1).joinToString("/"), json.asJsonObject[tokens[0]])
        } else {
            json.asJsonObject[tokens[0]]
        }
    }

    inline fun <reified T> map(data: JsonElement): T {
        return graph.objectMapper.readValue(data.toString(),
            object : TypeReference<T>() {

            })
    }

}