package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.OlympusEthereumService
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class GOHMMarketProvider(
    private val olympusEthereumService: OlympusEthereumService,
) : FarmingMarketProvider() {

    val ohmAddress = "0x64aa3364F17a4D01c6f1751Fd97C2BD3D7e7f1D5"
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val ohm =  getToken(ohmAddress)
        val gohm = olympusEthereumService.getGOHMContract()

        return listOf(
            create(
                name = "gohm",
                identifier = "gohm",
                stakedToken = ohm.toFungibleToken(),
                rewardTokens = listOf(ohm.toFungibleToken()),
                vaultType = "gohm",
                balanceFetcher = PositionFetcher(
                    gohm.address,
                    { user ->
                        getERC20Resource().balanceOfFunction(gohm.address, user, getNetwork())
                    },
                    { retVal ->
                        val gohmAmount = retVal[0].value as BigInteger
                        gohm.balanceFrom(gohmAmount)
                    }
                ),
                farmType = ContractType.STAKING
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