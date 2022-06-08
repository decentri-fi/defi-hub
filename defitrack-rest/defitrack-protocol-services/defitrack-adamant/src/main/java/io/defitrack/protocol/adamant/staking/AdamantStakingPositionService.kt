package io.defitrack.protocol.adamant.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.staking.StakingPositionService
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AdamantStakingPositionService(
    erC20Resource: ERC20Resource,
    private val adamantVaultMarketService: AdamantVaultMarketService,
) : StakingPositionService(
    erC20Resource
) {

    override suspend fun getStakings(address: String): List<StakingPosition> {

        val markets = adamantVaultMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.contractAddress }, getNetwork())
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val vault = markets[index]

                    StakingPosition(
                        vault,
                        balance
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ADAMANT
    }

    override fun getNetwork(): Network = Network.POLYGON
}