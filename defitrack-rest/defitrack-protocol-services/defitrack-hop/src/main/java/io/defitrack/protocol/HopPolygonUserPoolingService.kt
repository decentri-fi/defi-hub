package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class HopPolygonUserPoolingService(
    private val hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val erC20Resource: ERC20Resource
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val markets = hopPolygonPoolingMarketService.getPoolingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.address }, contractAccessorGateway.getGateway(getNetwork()))
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ONE) {
                    val pool = markets[index]
                    PoolingElement(
                        lpAddress = pool.address,
                        amount = balance.toBigDecimal(),
                        name = pool.token.map { it.symbol }.joinToString { "/" } + " LP",
                        symbol = pool.token.map { it.symbol }.joinToString { "-" },
                        network = getNetwork(),
                        protocol = getProtocol(),
                        TokenType.HOP,
                        pool.id
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