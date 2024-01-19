package io.defitrack.protocol.rocketpool

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.evm.position.Position
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.reth.RETHContract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.ROCKETPOOL)
class RocketpoolRethMarketProvider : FarmingMarketProvider() {

    val rethAddress = "0xae78736cd615f374d3085123a210448e74fc6393"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val eth = getToken("0x0")
        val reth = RETHContract(
            getBlockchainGateway(), rethAddress
        )

        return create(
            name = "rETH",
            identifier = rethAddress,
            stakedToken = eth,
            rewardToken = eth,
            token = getToken(reth.address),
            positionFetcher = PositionFetcher(
                reth::balanceOfFunction,
            ) {
                val bal = it[0].value as BigInteger
                if (bal > BigInteger.ZERO) {
                    Position(
                        reth.exchangeRate.await().times(bal).asEth().toBigInteger(),
                        bal,
                    )
                } else {
                    Position.ZERO
                }
            },
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ROCKETPOOL
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}