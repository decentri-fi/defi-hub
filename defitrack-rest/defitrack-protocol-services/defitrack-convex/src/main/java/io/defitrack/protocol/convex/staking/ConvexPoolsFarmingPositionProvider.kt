package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.protocol.FarmType
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexPoolsFarmingPositionProvider(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource,
) : FarmingMarketProvider() {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxRewardPool.json")
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())

        return convexService.providePools().map {
            CvxRewardPoolContract(
                gateway,
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }.map {
            val stakingToken = erC20Resource.getTokenInformation(getNetwork(), it.stakingToken())
            val rewardToken = erC20Resource.getTokenInformation(getNetwork(), it.rewardToken())
            create(
                name = it.name,
                identifier = it.name,
                stakedToken = stakingToken.toFungibleToken(),
                rewardTokens = listOf(rewardToken.toFungibleToken()),
                vaultType = "cvx-vault",
                marketSize = null,
                apr = null,
                balanceFetcher = null,
                farmType = FarmType.STAKING
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