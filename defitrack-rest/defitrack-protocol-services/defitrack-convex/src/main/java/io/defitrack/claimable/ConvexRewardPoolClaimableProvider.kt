package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.protocol.convex.staking.ConvexRewardPoolMarketProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexRewardPoolClaimableProvider(
    private val convexRewardPoolMarketProvider: ConvexRewardPoolMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource
) : ClaimableRewardProvider() {
    override suspend fun claimables(address: String): List<Claimable> {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())
        val markets = convexRewardPoolMarketProvider.getMarkets()

        return gateway.readMultiCall(
            markets.map {
                val contract = it.metadata["contract"] as CvxRewardPoolContract
                val function = contract.earnedFunction(address)
                MultiCallElement(
                    function, contract.address
                )
            }
        ).mapIndexed { index, retVal ->
            val amount = retVal[0].value as BigInteger
            if (amount > BigInteger.ZERO) {
                val market = markets[index]
                val contract = market.metadata["contract"] as CvxRewardPoolContract

                val rewardToken = erC20Resource.getTokenInformation(
                    getNetwork(), contract.rewardToken()
                )

                Claimable(
                    id = "${market.id}-claimable-$address",
                    name = "${market.name} reward",
                    type = "cvx_reward",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableTokens = listOf(
                        rewardToken.toFungibleToken()
                    ),
                    amount = amount,
                    claimTransaction = PreparedTransaction(
                        getNetwork().toVO(),
                        contract.getRewardFunction(address),
                        contract.address
                    )
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return convexRewardPoolMarketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return convexRewardPoolMarketProvider.getNetwork()
    }
}