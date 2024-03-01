package io.defitrack.protocol.application.compound.rewards

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.compound.CompoundAddressesProvider
import io.defitrack.protocol.compound.v3.contract.CompoundRewardContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.COMPOUND)
class CompoundEthereumRewardProvider(
    private val compoundAddressesProvider: CompoundAddressesProvider
) : CompoundRewardProvider(Network.ETHEREUM) {

    override fun getContract(): CompoundRewardContract = with(getBlockchainGateway()) {
        return object :
            CompoundRewardContract(compoundAddressesProvider.CONFIG[network]!!.rewards) {
            override suspend fun getRewardConfig(comet: String): RewardConfig {
                return (read(
                    "rewardConfig",
                    inputs = listOf(comet.toAddress()),
                    outputs = listOf(
                        TypeUtils.address(),
                        TypeUtils.uint64(),
                        TypeUtils.bool(),
                    )
                )[0].value as String).let {
                    RewardConfig(it)
                }
            }
        }
    }
}