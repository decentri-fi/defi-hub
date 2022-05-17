package io.defitrack.protocol.quickswap.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapDualUserStakingService(
    contractAccessorGateway: ContractAccessorGateway,
    quickswapStakingMarketService: QuickswapDualStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, quickswapStakingMarketService, contractAccessorGateway) {

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}