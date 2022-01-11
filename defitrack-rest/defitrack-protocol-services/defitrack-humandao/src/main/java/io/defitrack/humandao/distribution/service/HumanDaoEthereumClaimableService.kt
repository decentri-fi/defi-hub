package io.defitrack.humandao.distribution.service

import io.defitrack.claimable.ClaimableElement
import io.defitrack.claimable.ClaimableService
import io.defitrack.claimable.ClaimableToken
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class HumanDaoEthereumClaimableService(
    private val bonusDistributionService: BonusDistributionService,
) : ClaimableService {

    override fun claimables(address: String): List<ClaimableElement> {
        val status = bonusDistributionService.getBonusDistributionStatus(getNetwork(), address)
        return if (BigDecimal(status.currentBonusAmount) > BigDecimal.ZERO && !status.claimed) {
            listOf(
                ClaimableElement(
                    "humandao-bonus-distribution",
                    "Bonus Distribution",
                    address,
                    "humandao-bonus-distribution",
                    getProtocol(),
                    getNetwork(),
                    ClaimableToken(
                        "HumanDAO",
                        "HDAO",
                        (BigDecimal(status.currentBonusAmount).divide(BigDecimal.TEN.pow(18))).toDouble()
                    )
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