package io.defitrack.protocol.maple

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.maple.domain.MapleReserve
import io.defitrack.protocol.maple.domain.UserReserve
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class MapleV2MainnetService(
    graphGatewayProvider: TheGraphGatewayProvider,
    private val objectMapper: ObjectMapper,
) {

    val thegraph =
        graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/maple/protocol-v3")

    fun getLendingPoolAddressesProvider(): String {
        return "0xe5D0Ef77AED07C302634dC370537126A2CD26590"
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

    suspend fun getReserves(): List<MapleReserve> {
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
            object : TypeReference<List<MapleReserve>>() {

            })
    }
}
