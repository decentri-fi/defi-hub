package io.defitrack.protocol.camelot.claiming

import io.defitrack.claim.AbstractClaimableMarketProvider
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.ClaimableMarket
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.camelot.CamelotAdvisorVestingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotAdvisorVestingClaimableMarketProvider : AbstractClaimableMarketProvider() {

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
                claimableRewardFetchers = listOf(ClaimableRewardFetcher(
                    Reward(
                        xgrail,
                        getRewardFunction = contract.releasableFunction(),
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
                    preparedTransaction = selfExecutingTransaction(contract.releasableFunction())
                ))
            )
        )
    }
}