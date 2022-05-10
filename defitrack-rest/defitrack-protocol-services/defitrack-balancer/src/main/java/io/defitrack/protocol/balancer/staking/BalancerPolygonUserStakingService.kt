package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class BalancerPolygonUserStakingService(
    private val balancerPolygonStakingMarketService: BalancerPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {


    override fun getStakings(address: String): List<StakingElement> {

        val markets = balancerPolygonStakingMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.contractAddress }, getNetwork())
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ONE) {
                    val market = markets[index]
                    StakingElement(
                        amount = balance,
                        market = market
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}