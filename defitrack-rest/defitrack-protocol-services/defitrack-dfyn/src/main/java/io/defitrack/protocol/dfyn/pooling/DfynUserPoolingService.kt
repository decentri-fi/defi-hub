package io.defitrack.protocol.dfyn.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class DfynUserPoolingService(
    private val uniswapService: DfynService,
    private val erC20Resource: ERC20Resource,
    private val polygonContractAccessor: PolygonContractAccessor,
) : UserPoolingService() {


    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val allPairs = uniswapService.getPairs()

        return erC20Resource.getBalancesFor(address, allPairs.map { it.id }, polygonContractAccessor)
            .mapIndexed { index, balance ->


                if (balance > BigInteger.ZERO) {
                    val want = allPairs[index]

                    val token = erC20Resource.getTokenInformation(getNetwork(), want.id)

                    val token1 = want.token0
                    val token2 = want.token1
                    val amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(token.decimals))

                    PoolingElement(
                        lpAddress = token.address,
                        amount = amount,
                        name = "${token1.name}/${token2.name} LP",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        symbol = "${token1.symbol}/${token2.symbol}",
                        tokenType = TokenType.UNISWAP,
                        id = "dfyn-polygon-${want.id}",
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}