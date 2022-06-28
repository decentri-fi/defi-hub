package io.defitrack.humandao.distribution.service

import io.defitrack.claimable.Claimable
import io.defitrack.claimable.ClaimableService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class HumanDaoPolygonClaimableService(
    private val bonusDistributionService: BonusDistributionService,
    private val erC20Resource: ERC20Resource
) : ClaimableService {

    val hdao by lazy {
        runBlocking(Dispatchers.IO) {
            erC20Resource.getTokenInformation(getNetwork(), "0x72928d5436ff65e57f72d5566dcd3baedc649a88")
        }
    }

    override suspend fun claimables(address: String): List<Claimable> {
        val status = bonusDistributionService.getBonusDistributionStatus(getNetwork(), address)
        return if (BigDecimal(status.currentBonusAmount) > BigDecimal.ZERO && !status.claimed) {
            listOf(
                Claimable(
                    "humandao-bonus-distribution",
                    "Bonus Distribution",
                    address,
                    "humandao-bonus-distribution",
                    getProtocol(),
                    getNetwork(),
                    claimableToken = hdao.toFungibleToken(),
                    amount = BigInteger(status.currentBonusAmount)
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
        return Network.POLYGON
    }
}