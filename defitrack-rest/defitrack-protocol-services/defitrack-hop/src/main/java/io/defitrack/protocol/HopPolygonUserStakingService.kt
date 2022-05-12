package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class HopPolygonUserStakingService(
    private val hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    private val gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {
    override fun getStakings(address: String): List<StakingElement> {
        val markets = hopPolygonStakingMarketService.getStakingMarkets().filter {
            it.balanceFetcher != null
        }

        return gateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val balance = market.balanceFetcher!!.extractBalance(retVal)

            if (balance > BigInteger.ONE) {
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