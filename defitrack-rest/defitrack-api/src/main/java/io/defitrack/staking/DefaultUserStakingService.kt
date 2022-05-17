package io.defitrack.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import java.math.BigInteger

abstract class DefaultUserStakingService(
    erC20Resource: ERC20Resource,
    val stakingMarketService: StakingMarketService,
    val gateway: ContractAccessorGateway
) : UserStakingService(erC20Resource) {
    override fun getStakings(address: String): List<StakingElement> {
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
                stakingElement(
                    id = market.id,
                    vaultName = market.name,
                    vaultAddress = market.contractAddress,
                    vaultType = market.vaultType,
                    apr = market.apr,
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
        return stakingMarketService.getProtocol()
    }

    override fun getNetwork(): Network {
        return stakingMarketService.getNetwork()
    }
}