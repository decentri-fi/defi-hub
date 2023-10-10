package io.defitrack.protocol.olympusdao.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ERC20Contract.Companion.balanceOfFunction
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.Position
import io.defitrack.market.lending.domain.PositionFetcher
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
                stakedToken = ohm.toFungibleToken(),
                rewardTokens = listOf(ohm.toFungibleToken()),
                balanceFetcher = PositionFetcher(
                    gohm.address,
                    { user ->
                        balanceOfFunction(user)
                    },
                    { retVal ->
                        val gohmAmount = retVal[0].value as BigInteger
                        Position(
                            underlyingAmount = gohm.balanceFrom(gohmAmount),
                            tokenAmount = gohmAmount,
                        )
                    }
                ),
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.OLYMPUSDAO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}