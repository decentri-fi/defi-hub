package io.defitrack.staking

import io.defitrack.abi.ABIResource
import io.defitrack.apr.MinichefStakingAprCalculator
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomStakingMarketService(
    private val spiritFantomService: SpiritFantomService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : StakingMarketService() {

    override suspend fun fetchStakingMarkets(): List<StakingMarket> {
        val masterchef = MasterchefLpContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
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
            StakingMarket(
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
                apr = aprCalculator.calculateApr(),
                balanceFetcher = StakingMarketBalanceFetcher(
                    masterchef.address,
                    { user -> masterchef.userInfoFunction(index, user) }
                )
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