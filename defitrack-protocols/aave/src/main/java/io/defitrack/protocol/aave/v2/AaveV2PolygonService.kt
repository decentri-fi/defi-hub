package io.defitrack.protocol.aave.v2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.domain.UserReserve
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class AaveV2PolygonService(
    theGraphGatewayProvider: TheGraphGatewayProvider,
    private val objectMapper: ObjectMapper,
) {

    val thegraph =
        theGraphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/aave/aave-v2-matic")


    fun getLendingPoolAddressesProvider(): String {
        return "0xd05e3E715d945B59290df0ae8eF85c1BdB684744"
    }

    fun getLendingPoolDataProviderContract(): String {
        return "0x7551b5D2763519d4e37e8B81929D336De671d46d"
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

        val poolSharesAsString = thegraph.performQuery(query).asJsonObject["userReserves"].toString()
        return objectMapper.readValue(poolSharesAsString,
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

        val reservesAsString = thegraph.performQuery(query).asJsonObject["reserves"].toString()
        return objectMapper.readValue(reservesAsString,
            object : TypeReference<List<AaveReserve>>() {

            })
    }
}