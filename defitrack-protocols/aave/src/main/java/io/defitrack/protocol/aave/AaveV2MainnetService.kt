package io.defitrack.protocol.aave

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.aave.domain.AaveReserve
import io.defitrack.protocol.aave.domain.UserReserve
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class AaveV2MainnetService(
    graphGatewayProvider: TheGraphGatewayProvider,
    private val objectMapper: ObjectMapper,
) {

    val thegraph =
        graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/aave/protocol-v2")

    fun getLendingPoolAddressesProvider(): String {
        return "0xb53c1a33016b2dc2ff3653530bff1848a515c8c5"
    }

    suspend fun getUserReserves(user: String): List<UserReserve> {
        val query = """
           { 
              userReserves(where: {user: "$user"}) {
                reserve {
                  id
                  underlyingAsset
                  name
                  symbol
                  decimals
                  liquidityRate
                  variableBorrowRate
                },
                currentATokenBalance
                currentVariableDebt
                currentStableDebt                
              }
            }
        """.trimIndent()

        val userReservesAsString =
            thegraph.performQuery(query).asJsonObject["userReserves"].toString()
        return objectMapper.readValue(userReservesAsString,
            object : TypeReference<List<UserReserve>>() {

            })
    }

    suspend fun getReserves(): List<AaveReserve> {
        val query = """
            {
              reserves {
                  id
                  underlyingAsset
                  name
                  symbol
                  decimals
                  liquidityRate
                  variableBorrowRate
                  totalLiquidity
              }
            }
        """.trimIndent()

        val reservesAsString =
            thegraph.performQuery(query).asJsonObject["reserves"].toString()
        return objectMapper.readValue(reservesAsString,
            object : TypeReference<List<AaveReserve>>() {

            })
    }
}