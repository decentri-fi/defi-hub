package io.defitrack.protocol.ondo

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.Position
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.ONDO)
class OUSGStakingMarketProvider : FarmingMarketProvider() {

    val cashManagerAddress = "0x3501883a646f1f8417bcb62162372550954d618f"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val manager = OndoCashManager(getBlockchainGateway(), cashManagerAddress)
        val collateral = getToken(manager.collateral.await())
        val cash = getToken(manager.cash.await())

        return create(
            name = "OUSG Staking",
            identifier = cashManagerAddress,
            stakedToken = collateral,
            rewardToken = collateral,
            positionFetcher = PositionFetcher(
                cash.asERC20Contract(getBlockchainGateway())::balanceOfFunction
            ) {
                val bal = it[0].value as BigInteger
                if (bal > BigInteger.ZERO) {
                    Position(
                        bal.times(manager.lastSetMintExchangeRate.await()).asEth(19 + 12).toBigInteger(),
                        bal
                    )
                } else {
                    Position.ZERO
                }
            },
        ).nel()
    }
    override fun getProtocol(): Protocol {
        return Protocol.ONDO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}