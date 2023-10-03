package io.defitrack.protocol.convex.claimable

import io.defitrack.claimable.UserClaimable
import io.defitrack.claimable.UserClaimableProvider
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.convex.contract.CvxRewardPoolContract
import io.defitrack.protocol.convex.staking.ConvexEthereumRewardPoolMarketProvider
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.CONVEX)
class ConvexRewardPoolClaimableProvider(
    private val convexEthereumRewardPoolMarketProvider: ConvexEthereumRewardPoolMarketProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : UserClaimableProvider() {
    override suspend fun claimables(address: String): List<UserClaimable> {
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

                UserClaimable(
                    id = "${market.id}-claimable-$address",
                    name = "${market.name} reward",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableToken = rewardToken.toFungibleToken(),
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