package io.defitrack.uniswap

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.defitrack.uniswap.domain.PairDayData
import io.defitrack.uniswap.domain.UniswapPair
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.days

abstract class AbstractUniswapV2Service(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) {

    val gateway = graphGatewayProvider.createTheGraphGateway(getGraphUrl())

    fun getPairDayData(pairId: String): List<PairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()

        val response = gateway.performQuery(query)
        val poolSharesAsString = response.asJsonObject["pairDayDatas"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<PairDayData>>() {

            })
    }

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<UniswapPair>>()

    fun getPairs(): List<UniswapPair> = runBlocking {
        pairCache.get("all") {
            val query = """
            {
            	pairs(first: 500, orderDirection: desc, orderBy: reserveUSD) {
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

            val response = gateway.performQuery(query)
            val poolSharesAsString = response.asJsonObject["pairs"].toString()
            objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<UniswapPair>>() {

                })
        }
    }

    abstract fun getGraphUrl(): String
    abstract fun getNetwork(): Network
}