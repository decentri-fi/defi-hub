package io.defitrack.protocol.crv

import io.defitrack.protocol.crv.domain.Gauge
import io.defitrack.protocol.crv.domain.Pool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class CurveEthereumService(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/curvefi/curve", theGraphGatewayProvider
) {

    suspend fun getPools(): List<Pool> {
        val query = """
            {
            	pools {
                id
                fee
                name
                swapAddress
                virtualPrice
                lpToken {
                  id
                  address
                  decimals
                  name
                	symbol
                }
                coins {
                  id
                  index
                  underlying {
                    id
                    index
                    balance,
                    token {
                      address
                      decimals
                      name
                      symbol
                    }
                  }
                }
              }
            }
        """.trimIndent()

        return query(query, "pools")
    }

    suspend fun getGauges(): List<Gauge> {
        val query = """
            {
            	gauges {
                address
                pool {
                     id
                     name
                     swapAddress
                     virtualPrice
                     underlyingCount
                     lpToken {
                       id
                       address
                       decimals
                       name
                     	symbol
                     }
                     coins {
                       id
                       index
                       token {
                         address
                         decimals
                         name
                         symbol
                       }
                       underlying {
                         id
                         index
                         balance,
                         token {
                           address
                           decimals
                           name
                           symbol
                         }
                       }
                     }            
                }
              }
            }
        """.trimIndent()

        return query(query, "gauges")
    }
}