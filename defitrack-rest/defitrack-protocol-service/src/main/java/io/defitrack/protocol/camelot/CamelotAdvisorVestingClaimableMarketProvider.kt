package io.defitrack.protocol.camelot

import io.defitrack.claimable.ClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotAdvisorVestingClaimableMarketProvider(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
) : ClaimableMarketProvider() {

    val deferredContract = lazyAsync {
        CamelotAdvisorVestingContract(
            blockchainGatewayProvider.getGateway(Network.ARBITRUM),
            "0x8b4ee9a030c50fd02c845a171064f8fca90cb155"
        )
    }

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val contract = deferredContract.await()
        val xgrail = erC20Resource.getTokenInformation(Network.ARBITRUM, contract.xGrailToken.await())

        return listOf(
            ClaimableMarket(
                id = "rwrd_${contract.address}",
                name = "Camelot Advisor Vesting",
                network = Network.ARBITRUM,
                protocol = Protocol.CAMELOT,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        xgrail.toFungibleToken(),
                        contract.address,
                        getRewardFunction = { user ->
                            contract.releasableFunction()
                        },
                        extractAmountFromRewardFunction = { results, user ->
                            if (results[0].value as BigInteger > BigInteger.ZERO) {
                                val share = contract.beneficiariesShares(user)
                                val totalShare = contract.totalShare.await()
                                val ratio = share.toBigDecimal().dividePrecisely(totalShare.toBigDecimal())
                                (results[0].value as BigInteger).toBigDecimal().times(ratio).toBigInteger()
                            } else {
                                BigInteger.ZERO
                            }
                        }
                    ),
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            network = Network.ARBITRUM.toVO(),
                            function = contract.releasableFunction(),
                            to = contract.address,
                        )
                    }
                )
            )
        )
    }
}