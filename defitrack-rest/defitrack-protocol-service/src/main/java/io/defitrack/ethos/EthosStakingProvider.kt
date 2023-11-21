package io.defitrack.ethos

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.FarmingContract
import io.defitrack.market.farming.SingleContractFarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.ETHOS)
class EthosStakingProvider : SingleContractFarmingMarketProvider() {
    override suspend fun single(): SingleFarmingConfig {
        return SingleFarmingConfig(
            "Ethos Staking",
            object : FarmingContract(
                getBlockchainGateway(),
                "0x9425b96462b1940e7563cd765464300f6a774805",
                "lqtyToken",
                "lusdToken",
                "getPendingLUSDGain",
            ) {
                override fun claimFn(user: String): MutableFunction {
                    return createFunction(
                        "unstake",
                        listOf(BigInteger.ZERO.toUint256())
                    ).toMutableFunction()
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