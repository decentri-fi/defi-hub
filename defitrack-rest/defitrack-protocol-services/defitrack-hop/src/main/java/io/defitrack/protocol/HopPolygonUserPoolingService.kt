package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger

@Component
class HopPolygonUserPoolingService(
    private val hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val erC20Resource: ERC20Resource
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val markets = hopPolygonPoolingMarketService.getPoolingMarkets()

        return erC20Resource.getBalancesFor(
            address,
            markets.map { it.address },
            getNetwork()
        )
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ONE) {
                    val pool = markets[index]

                    val tokenInfo = erC20Resource.getTokenInformation(getNetwork(), pool.address)

                    PoolingElement(
                        lpAddress = pool.address,
                        amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(tokenInfo.decimals)),
                        name = pool.token.joinToString("/") { it.symbol } + " LP",
                        symbol = tokenInfo.symbol,
                        network = getNetwork(),
                        protocol = getProtocol(),
                        tokenType = TokenType.HOP,
                        id = pool.id
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}