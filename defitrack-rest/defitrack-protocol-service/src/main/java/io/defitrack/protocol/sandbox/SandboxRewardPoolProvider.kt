package io.defitrack.protocol.sandbox

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.FarmingContract
import io.defitrack.market.port.out.SingleContractFarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SANDBOX)
class SandboxRewardPoolProvider : SingleContractFarmingMarketProvider() {
    val contract = "0xa6e383bda26e4c52a3a3a3463552c42494669abd"

    override suspend fun single(): SingleFarmingConfig {
        return SingleFarmingConfig(
            contract,
            FarmingContract(
                getBlockchainGateway(),
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