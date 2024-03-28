package io.defitrack.protocol.application.olympusdao

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.olympusdao.OlympusEthereumService
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.OLYMPUSDAO)
class GOHMMarketProvider(
    private val olympusEthereumService: OlympusEthereumService,
) : FarmingMarketProvider() {

    val ohmAddress = "0x64aa3364F17a4D01c6f1751Fd97C2BD3D7e7f1D5"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val ohm = getToken(ohmAddress)
        val gohm = olympusEthereumService.getGOHMContract()

        return listOf(
            create(
                name = "gohm",
                identifier = "gohm",
                stakedToken = ohm,
                rewardToken = ohm,
                type = "olympusdao.gohm",
                positionFetcher = PositionFetcher(gohm::balanceOfFunction)
                { retVal ->
                    val gohmAmount = retVal[0].value as BigInteger
                    if (gohmAmount > BigInteger.ZERO) {
                        Position(
                            underlyingAmount = gohm.balanceFrom(gohmAmount),
                            tokenAmount = gohmAmount,
                        )
                    } else Position.ZERO
                }
            ),
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.OLYMPUSDAO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}