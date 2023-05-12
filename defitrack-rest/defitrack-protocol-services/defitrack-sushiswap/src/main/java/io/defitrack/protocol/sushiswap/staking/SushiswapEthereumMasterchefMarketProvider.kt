package io.defitrack.protocol.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.MasterChefBasedContract
import io.defitrack.protocol.contract.MasterChefPoolInfo
import io.defitrack.protocol.sushiswap.apr.MasterchefBasedfStakingAprCalculator
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class SushiswapEthereumMasterchefMarketProvider : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = MasterChefBasedContract(
            "sushi",
            "sushiPerBlock",
            "pendingSushi",
            getBlockchainGateway(),
            "0xc2EdaD668740f1aA35E4D8f227fB8E17dcA888Cd"
        )

        contract.poolInfos().mapIndexed { poolIndex, poolInfo ->
            async {
                throttled {
                    toStakingMarketElement(poolInfo, contract, poolIndex)
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private suspend fun toStakingMarketElement(
        poolInfo: MasterChefPoolInfo,
        chef: MasterChefBasedContract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(poolInfo.lpToken)
            val rewardToken = getToken(chef.rewardToken())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardToken.toFungibleToken()
                ),
                vaultType = "sushi-masterchef",
                marketSize = getMarketSize(stakedtoken.toFungibleToken(), chef.address),
                apr = MasterchefBasedfStakingAprCalculator(
                    getERC20Resource(),
                    getPriceResource(),
                    chef,
                    poolId
                ).calculateApr(),
                balanceFetcher = PositionFetcher(
                    chef.address,
                    { user -> chef.userInfoFunction(poolId, user) }
                ),
                farmType = ContractType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}