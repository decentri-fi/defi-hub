package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class SpiritFantomUserPoolingService(
    private val spiritFantomPoolingMarketService: SpiritFantomPoolingMarketService,
    private val erC20Resource: ERC20Resource,
) : UserPoolingService() {
    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val markets = spiritFantomPoolingMarketService.getPoolingMarkets()
        return erC20Resource.getBalancesFor(
            address,
            markets.map {
                it.address
            }, getNetwork()
        ).mapIndexed { index, balance ->
            if (balance > BigInteger.ZERO) {
                val market = markets[index]
                val token = erC20Resource.getTokenInformation(getNetwork(), market.address)
                poolingElement(
                    market = market,
                    amount = balance.asEth(token.decimals),
                )
            } else null
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}