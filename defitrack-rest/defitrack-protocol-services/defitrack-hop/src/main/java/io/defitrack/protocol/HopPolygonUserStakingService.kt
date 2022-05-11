package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class HopPolygonUserStakingService(
    private val hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {
    override fun getStakings(address: String): List<StakingElement> {
        val markets = hopPolygonStakingMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(
            address,
            markets.map {
                it.contractAddress
            },
            getNetwork()
        ).mapIndexed { index, balance ->
            if (balance > BigInteger.ONE) {
                val market = markets[index]
                stakingElement(
                    id = market.id,
                    vaultName = market.name,
                    vaultAddress = market.contractAddress,
                    vaultType = market.vaultType,
                    rate = market.rate,
                    stakedToken = market.stakedToken,
                    amount = balance,
                    rewardTokens = market.rewardTokens,
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