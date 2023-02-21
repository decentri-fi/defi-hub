package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import org.springframework.stereotype.Service

@Service
class ConvexRewardPoolMarketProvider(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
) : FarmingMarketProvider() {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxRewardPool.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        return convexService.providePools().map {
            CvxRewardPoolContract(
                getBlockchainGateway(),
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }.map {
            val stakingToken = getToken(it.stakingToken())
            val rewardToken = getToken(it.rewardToken())
            create(
                name = it.name,
                identifier = it.name,
                stakedToken = stakingToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                vaultType = "cvx-vault",
                marketSize = null,
                apr = null,
                balanceFetcher = defaultPositionFetcher(
                    it.address
                ),
                farmType = FarmType.STAKING,
                metadata = mapOf("contract" to it)
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}