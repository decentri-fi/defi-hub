package io.defitrack.protocol.dinoswap.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.UserStakingService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DinoswapUserStakingService(
    private val dinoswapStakingMarketService: DinoswapStakingMarketService,
    erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : UserStakingService(erC20Resource) {


    override fun getStakings(address: String): List<StakingElement> {
        val markets = dinoswapStakingMarketService.getStakingMarkets()

        return contractAccessorGateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val bal = market.balanceFetcher!!.extractBalance(retVal)
            if (bal > BigInteger.ONE) {
                StakingElement(
                    market,
                    bal
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DINOSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}