package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.protocol.convex.staking.ConvexEthereumRewardPoolMarketProvider
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ConvexRewardPoolClaimableProvider(
    private val convexEthereumRewardPoolMarketProvider: ConvexEthereumRewardPoolMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ClaimableRewardProvider() {
    override suspend fun claimables(address: String): List<Claimable> {
        val gateway = blockchainGatewayProvider.getGateway(getNetwork())
        val markets = convexEthereumRewardPoolMarketProvider.getMarkets()

        return gateway.readMultiCall(
            markets.map {
                val contract = it.internalMetadata["contract"] as CvxRewardPoolContract
                val function = contract.earnedFunction(address)
                MultiCallElement(
                    function, contract.address
                )
            }
        ).mapIndexed { index, retVal ->
            val amount = retVal.data[0].value as BigInteger
            if (amount > BigInteger.ZERO) {
                val market = markets[index]
                val contract = market.internalMetadata["contract"] as CvxRewardPoolContract

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
        return convexEthereumRewardPoolMarketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return convexEthereumRewardPoolMarketProvider.getNetwork()
    }
}