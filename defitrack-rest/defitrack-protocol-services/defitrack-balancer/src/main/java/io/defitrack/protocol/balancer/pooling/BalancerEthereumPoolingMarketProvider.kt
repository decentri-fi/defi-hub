package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.graph.BalancerEthereumPoolGraphProvider
import io.defitrack.protocol.balancer.graph.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class BalancerEthereumPoolingMarketProvider(
    balancerEthereumPoolGraphProvider: BalancerEthereumPoolGraphProvider,
) : BalancerPoolingMarketProvider(balancerEthereumPoolGraphProvider) {


    override fun getNetwork(): Network = Network.ETHEREUM
}