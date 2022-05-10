package io.defitrack.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomStakingMarketService(
    private val spiritFantomService: SpiritFantomService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    override suspend fun fetchStakingMarkets(): List<StakingMarketElement> {
        val masterchef = MasterchefLpContract(
            contractAccessorGateway.getGateway(getNetwork()),
            abiResource.getABI("spirit/Masterchef.json"),
            spiritFantomService.getMasterchef()
        )

        val reward = erC20Resource.getTokenInformation(getNetwork(), masterchef.rewardToken)

        return masterchef.poolInfos.mapIndexed { index, value ->

            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), value.lpToken)
            val aprCalculator = MinichefStakingAprCalculator(
                erC20Resource,
                priceResource,
                masterchef,
                index,
                stakedToken
            )
            StakingMarketElement(
                id = "fantom-spirit-${masterchef.address}-${index}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} spirit farm",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(
                    reward.toFungibleToken()
                ),
                contractAddress = masterchef.address,
                vaultType = "spirit-masterchef",
                marketSize = BigDecimal.ZERO,
                rate = aprCalculator.calculateApr()
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}