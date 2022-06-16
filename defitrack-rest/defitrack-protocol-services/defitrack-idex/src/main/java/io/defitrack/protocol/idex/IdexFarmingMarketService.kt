package io.defitrack.protocol.idex

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class IdexFarmingMarketService(
    private val abiResource: ABIResource,
    private val tokenService: ERC20Resource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val idexService: IdexService
) : FarmingMarketService() {


    val minichefABI by lazy {
        abiResource.getABI("idex/IdexFarm.json")
    }

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> = coroutineScope {
        idexService.idexFarm().map {
            IdexFarmContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                minichefABI,
                it
            )
        }.flatMap { chef ->
            (0 until chef.poolLength).map { poolId ->
                async(Dispatchers.IO.limitedParallelism(10)) {
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

    private fun toStakingMarketElement(
        chef: IdexFarmContract,
        poolId: Int
    ): FarmingMarket {
        val stakedtoken =
            tokenService.getTokenInformation(getNetwork(), chef.getLpTokenForPoolId(poolId))
        val rewardToken = tokenService.getTokenInformation(getNetwork(), chef.rewardToken)
        return FarmingMarket(
            id = "idex-${chef.address}-${poolId}",
            network = getNetwork(),
            name = stakedtoken.name + " Farm",
            protocol = getProtocol(),
            stakedToken = stakedtoken.toFungibleToken(),
            rewardTokens = listOf(
                rewardToken.toFungibleToken()
            ),
            contractAddress = chef.address,
            vaultType = "idex-farm",
            balanceFetcher = FarmingPositionFetcher(
                chef.address,
                { user -> chef.userInfoFunction(poolId, user) }
            )
        )
    }
}