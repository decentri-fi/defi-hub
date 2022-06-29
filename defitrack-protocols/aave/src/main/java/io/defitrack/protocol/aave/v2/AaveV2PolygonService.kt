package io.defitrack.protocol.aave.v2

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.domain.UserReserve
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class AaveV2PolygonService(
    theGraphGatewayProvider: TheGraphGatewayProvider,
    private val objectMapper: ObjectMapper,
) : GraphProvider("https://api.thegraph.com/subgraphs/name/aave/aave-v2-matic", theGraphGatewayProvider) {

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

        return query(query, "userReserves")
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
                  aToken {
                    id
                  }
              }
            }
        """.trimIndent()

        return query(query, "reserves")
    }
}