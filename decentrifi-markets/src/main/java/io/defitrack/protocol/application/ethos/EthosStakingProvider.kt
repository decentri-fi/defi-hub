package io.defitrack.protocol.application.ethos

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.FarmingContract
import io.defitrack.market.port.out.SingleContractFarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.ETHOS)
class EthosStakingProvider : SingleContractFarmingMarketProvider() {
    override suspend fun single(): SingleFarmingConfig {
        return SingleFarmingConfig(
            name = "Ethos Staking",
            type = "ethos.staking",
            contract = object : FarmingContract(
                getBlockchainGateway(),
                "0x9425b96462b1940e7563cd765464300f6a774805",
                "lqtyToken",
                "lusdToken",
                "getPendingLUSDGain",
            ) {
                override fun claimFn(user: String): ContractCall {
                    return createFunction(
                        "unstake",
                        listOf(BigInteger.ZERO.toUint256())
                    )
                }
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.ETHOS
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}