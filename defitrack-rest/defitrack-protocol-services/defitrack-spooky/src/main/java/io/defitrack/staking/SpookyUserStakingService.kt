package io.defitrack.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpookyFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class SpookyUserStakingService(
    private val spookyStakingMarketService: SpookyStakingMarketService,
    private val spookyFantomService: SpookyFantomService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
) : UserStakingService(erC20Resource) {

    override fun getStakings(address: String): List<StakingElement> {
        val gateway = contractAccessorGateway.getGateway(getNetwork())

        val masterchef = MasterchefLpContract(
            gateway,
            abiResource.getABI("spooky/Masterchef.json"),
            spookyFantomService.getMasterchef()
        )

        val stakingMarkets: List<StakingMarketElement> by lazy {
            spookyStakingMarketService.getStakingMarkets()
        }

        return gateway.readMultiCall(masterchef.userInfo(address))
            .mapIndexed { index, value ->
                val balance = value[0].value
                if ((balance as BigInteger) > BigInteger.ZERO) {
                    val stakingMarket = stakingMarkets[index]
                    stakingElement(
                        id = stakingMarket.id,
                        vaultName = stakingMarket.name,
                        vaultAddress = stakingMarket.contractAddress,
                        vaultType = stakingMarket.vaultType,
                        apr = stakingMarket.apr,
                        stakedToken = stakingMarket.stakedToken,
                        rewardTokens = stakingMarket.rewardTokens,
                        amount = balance
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPOOKY
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}