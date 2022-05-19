package io.defitrack.protocol.dinoswap.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.StakingPositionService
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DinoswapStakingPositionService(
    private val dinoswapStakingMarketService: DinoswapStakingMarketService,
    erC20Resource: ERC20Resource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingPositionService(erC20Resource) {


    override fun getStakings(address: String): List<StakingPosition> {
        val markets = dinoswapStakingMarketService.getStakingMarkets()

        return contractAccessorGateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val bal = market.balanceFetcher!!.extractBalance(retVal)
            if (bal > BigInteger.ONE) {
                StakingPosition(
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