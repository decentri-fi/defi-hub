package io.defitrack.humandao.distribution.service

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableRewardProvider
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class HumanDaoEthereumClaimableRewardProvider(
    private val bonusDistributionService: BonusDistributionService,
    private val erC20Resource: ERC20Resource
) : ClaimableRewardProvider {

    val hdao by lazy { runBlocking(Dispatchers.IO) {
        erC20Resource.getTokenInformation(getNetwork(), "0xdac657ffd44a3b9d8aba8749830bf14beb66ff2d")
    } }

    override suspend fun claimables(address: String): List<Claimable> {
        val status = bonusDistributionService.getBonusDistributionStatus(getNetwork(), address)
        return if (BigDecimal(status.currentBonusAmount) > BigDecimal.ZERO && !status.claimed) {


            listOf(
                Claimable(
                    id = "humandao-bonus-distribution",
                    name = "Bonus Distribution",
                    type = "humandao-bonus-distribution",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    claimableTokens = listOf(hdao.toFungibleToken()),
                    amount = BigInteger(status.currentBonusAmount),
                )
            )
        } else {
            emptyList()
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.HUMANDAO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}