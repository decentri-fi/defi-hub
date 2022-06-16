package io.defitrack.protocol.convex.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.ConvexService
import io.defitrack.protocol.convex.contract.CvxLockerContract
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexVoteLockedFarmingPositionProvider(
    private val convexService: ConvexService,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource
) :
    FarmingPositionProvider(erC20Resource) {

    val cvxRewardPoolABI by lazy {
        abiResource.getABI("convex/CvxLocker.json")
    }

    override suspend fun getStakings(address: String): List<FarmingPosition> {
        val cvxRewardPools = listOf(convexService.lockedRewardPool()).map {
            CvxLockerContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                cvxRewardPoolABI,
                it.address,
                it.name
            )
        }

        return cvxRewardPools.mapNotNull { pool ->
            val balance = pool.balances(address)
            if (balance > BigInteger.ZERO) {

                val rewardToken = erC20Resource.getTokenInformation(getNetwork(), pool.rewardToken())
                val stakedToken = erC20Resource.getTokenInformation(getNetwork(), pool.stakingToken())

                stakingElement(
                    id = "convex-ethereum-${pool.address}",
                    vaultName = pool.name,
                    rewardTokens = listOf(
                        rewardToken.toFungibleToken()
                    ),
                    stakedToken = stakedToken.toFungibleToken(),
                    vaultType = "convex-locked-vote",
                    vaultAddress = pool.address,
                    amount = balance
                )
            } else {
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CONVEX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}