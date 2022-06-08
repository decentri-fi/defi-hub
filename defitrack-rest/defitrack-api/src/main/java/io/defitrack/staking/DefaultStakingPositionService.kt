package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.token.ERC20Resource
import java.math.BigInteger

abstract class DefaultStakingPositionService(
    erC20Resource: ERC20Resource,
    val stakingMarketService: StakingMarketService,
    val gateway: ContractAccessorGateway
) : StakingPositionService(erC20Resource) {
    override suspend fun getStakings(address: String): List<StakingPosition> {
        val markets = stakingMarketService.getStakingMarkets().filter {
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
                StakingPosition(
                    market,
                    balance
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return stakingMarketService.getProtocol()
    }

    override fun getNetwork(): Network {
        return stakingMarketService.getNetwork()
    }
}