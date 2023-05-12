package io.defitrack.farming

import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateService
import io.defitrack.protocol.contract.LPStakingContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
abstract class StargateFarmingMarketProvider(
    private val stargateOptimismService: StargateService,
) : FarmingMarketProvider() {


    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    val lpStakingContractAbi by lazy {
        runBlocking {
            getAbi("stargate/LPStaking.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val lpStakingContract = LPStakingContract(
            getBlockchainGateway(),
            lpStakingContractAbi,
            stargateOptimismService.getLpFarm()
        )
        val stargate = getToken(lpStakingContract.stargate())

        return lpStakingContract.poolInfos().mapIndexed { index, info ->
            val stakedToken = getToken(info.lpToken)
            val rewardTokens = listOf(stargate)

            create(
                identifier = "${lpStakingContract.address}-$index",
                name = "Stargate ${stakedToken.name} Reward",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = rewardTokens.map { it.toFungibleToken() },
                vaultType = "stargate-lp-staking",
                farmType = ContractType.LIQUIDITY_MINING,
                balanceFetcher = PositionFetcher(
                    lpStakingContract.address,
                    { user: String -> lpStakingContract.userInfo(index, user) },
                )
            )
        }
    }
}