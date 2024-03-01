package io.defitrack.protocol.application.sandbox

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.FarmingContract
import io.defitrack.market.port.out.SingleContractFarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SANDBOX)
class SandboxRewardPoolProvider : SingleContractFarmingMarketProvider() {
    val contract = "0xa6e383bda26e4c52a3a3a3463552c42494669abd"

    context(BlockchainGateway)
    override suspend fun single(): SingleFarmingConfig {
        return SingleFarmingConfig(
            name = "Sandbox Reward Pool",
            type = "sandbox.erc20-reward-pool",
            contract = FarmingContract(
                contract,
                "stakeToken",
                "rewardToken",
                "earned",
                "getReward"
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.SANDBOX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}