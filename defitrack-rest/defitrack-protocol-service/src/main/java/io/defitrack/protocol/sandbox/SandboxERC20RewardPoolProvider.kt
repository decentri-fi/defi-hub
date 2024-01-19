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
class SandboxERC20RewardPoolProvider: SingleContractFarmingMarketProvider() {

    val contract = "0xd3a9caa25393765c05ce9f332b5e33b5e33d8b8f"

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