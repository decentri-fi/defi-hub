package io.defitrack.protocol.dmm

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.thegraph.TheGraphGatewayProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DMMEthereumService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider,
) {

    val graph =
        graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/dynamic-amm/dynamic-amm")

    fun getPairDayData(pairId: String): List<DMMPairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()
        val data = graph.performQuery(query).asJsonObject["pairDayDatas"]
        return@runBlocking graph.map<List<DMMPairDayData>>(data)
    }

    fun getPoolingMarkets(): List<DMMPool> = runBlocking {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                pair {
                    id
                }
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
        val data = graph.performQuery(query).asJsonObject["pools"]
        return@runBlocking graph.map(data)
    }
}