package io.defitrack.protocol.aave.v2

import io.defitrack.protocol.aave.v2.domain.AaveReserve
import io.defitrack.protocol.aave.v2.domain.UserReserve
import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class AaveV2MainnetService(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider("https://api.thegraph.com/subgraphs/name/aave/protocol-v2", graphGatewayProvider) {

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