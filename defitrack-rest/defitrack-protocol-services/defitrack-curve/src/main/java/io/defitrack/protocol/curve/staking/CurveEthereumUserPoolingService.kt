package io.defitrack.protocol.curve.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class CurveEthereumUserPoolingService(
    private val curveEthereumPoolingMarketService: CurveEthereumPoolingMarketService,
    private val erC20Resource: ERC20Resource
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val poolingMarkets = curveEthereumPoolingMarketService.getPoolingMarkets()
        return erC20Resource.getBalancesFor(
            address,
            poolingMarkets.map { it.address },
            getNetwork()
        ).mapIndexed { index, balance ->
            if (balance > BigInteger.ZERO) {

                val poolingMarket = poolingMarkets[index]
                val lpToken = erC20Resource.getTokenInformation(getNetwork(), poolingMarket.address)

                PoolingElement(
                    lpAddress = poolingMarket.address,
                    amount = balance.toBigDecimal(),
                    name = lpToken.name,
                    symbol = lpToken.symbol,
                    network = getNetwork(),
                    protocol = getProtocol(),
                    tokenType = TokenType.CURVE,
                    id = poolingMarket.id
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}