package io.defitrack.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.fantom.config.FantomContractAccessor
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
    private val fantomContractAccessor: FantomContractAccessor,
    private val abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    objectMapper: ObjectMapper
) : UserStakingService(erC20Resource, objectMapper) {

    override fun getStakings(address: String): List<StakingElement> {
        val masterchef = MasterchefLpContract(
            fantomContractAccessor,
            abiResource.getABI("spooky/Masterchef.json"),
            spookyFantomService.getMasterchef()
        )

        val stakingMarkets: List<StakingMarketElement> by lazy {
            spookyStakingMarketService.getStakingMarkets()
        }

        return fantomContractAccessor.readMultiCall(masterchef.userInfo(address)).mapIndexed { index, value ->
            val balance = value[0].value
            if ((balance as BigInteger) > BigInteger.ZERO) {
                val stakingMarket = stakingMarkets[index]
                StakingElement(
                    id = stakingMarket.id,
                    network = getNetwork(),
                    user = address,
                    protocol = getProtocol(),
                    name = stakingMarket.name,
                    contractAddress = stakingMarket.contractAddress,
                    vaultType = stakingMarket.vaultType,
                    rate = stakingMarket.rate.toDouble(),
                    url = "",
                    stakedToken = stakingMarket.token,
                    rewardTokens = stakingMarket.reward,
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