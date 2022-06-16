package io.defitrack.protocol.adamant.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.market.farming.FarmingPositionProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AdamantFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    private val adamantVaultMarketService: AdamantVaultMarketService,
) : FarmingPositionProvider(
    erC20Resource
) {

    override suspend fun getStakings(address: String): List<FarmingPosition> {

        val markets = adamantVaultMarketService.getStakingMarkets()

        return erC20Resource.getBalancesFor(address, markets.map { it.contractAddress }, getNetwork())
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val vault = markets[index]

                    FarmingPosition(
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