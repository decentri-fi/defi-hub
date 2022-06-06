package io.defitrack.protocol.dfyn

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.dfyn.domain.Pair
import io.defitrack.protocol.dfyn.domain.PairDayData
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
class DfynService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) {

    val endpoint = "https://api.thegraph.com/subgraphs/name/ss-sonic/dfyn-v5"
    val graph = graphGatewayProvider.createTheGraphGateway(endpoint)

    fun getPairDayData(pairId: String): List<PairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()

        val data = graph.performQuery(query).asJsonObject["pairDayDatas"].toString()
        return@runBlocking objectMapper.readValue(data,
            object : TypeReference<List<PairDayData>>() {

            })
    }

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<Pair>>()

    fun getPairs(): List<Pair> = runBlocking {
        pairCache.get("all") {
            val query = """
            {
            	pairs(first: 200, orderDirection: desc, orderBy: volumeUSD) {
                id
                reserveUSD
                token0 {
                  id,
                  symbol
                  name
                  decimals
                }
                token1 {
                  id,
                  symbol
                  name
                  decimals
                }                
              }
            }
        """.trimIndent()
            val poolSharesAsString = graph.performQuery(query).asJsonObject["pairs"].toString()
            objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<Pair>>() {

                })
        }
    }
}