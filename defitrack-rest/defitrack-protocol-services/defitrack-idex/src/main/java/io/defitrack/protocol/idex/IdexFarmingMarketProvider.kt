package io.defitrack.protocol.idex

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.token.DecentrifiERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class IdexFarmingMarketProvider(
    private val tokenService: DecentrifiERC20Resource,
    private val idexService: IdexService
) : FarmingMarketProvider() {


    val minichefABI by lazy {
        runBlocking {
            getAbi("idex/IdexFarm.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        idexService.idexFarm().map {
            IdexFarmContract(
                getBlockchainGateway(),
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength()).map { poolId ->
                async {
                    try {
                        toStakingMarketElement(chef, poolId)
                    } catch (ex: Exception) {
                        logger.debug("something went wrong trying to import idex pool", ex)
                        null
                    }
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX

    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    private suspend fun toStakingMarketElement(
        chef: IdexFarmContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken())
        return create(
            identifier = "${chef.address}-${poolId}",
            name = stakedtoken.name + " Farm",
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            vaultType = "idex-farm",
            balanceFetcher = PositionFetcher(
                chef.address,
                { user -> chef.userInfoFunction(poolId, user) }
            ),
            farmType = ContractType.LIQUIDITY_MINING
        )
    }
}