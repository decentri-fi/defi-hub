package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceResource
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class HopPolygonUserStakingService(
    private val hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
    private val abiResource: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val priceResource: PriceResource
) : UserStakingService(erC20Resource) {
    override fun getStakings(address: String): List<StakingElement> {
        val markets = hopPolygonStakingMarketService.getStakingMarkets()

        erC20Resource.getBalancesFor(
            address,
            markets.map {
                it.contractAddress
            },
            getNetwork()
        ).mapIndexed { index, balance ->
            if (balance > BigInteger.ONE) {
                val market = markets[index]
                StakingElement(
                    id = market.id,
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = market.name,
                    contractAddress = market.contractAddress,
                    vaultType = market.vaultType,
                    rate = market.rate.toDouble(),
                    stakedToken = market.token,
                    amount = balance,
                    rewardTokens = market.reward,
                )
            } else {
                null
            }
        }

        return emptyList()
    }


    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}