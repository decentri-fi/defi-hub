package io.defitrack.protocol.pendle

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@ConditionalOnNetwork(Network.ARBITRUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleArbitrumiquidityPoolMarketProvider : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {

        val factory = PendleMarketFactoryContract(
            getBlockchainGateway(),
            "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced"
        )

        return factory.getMarkets("154873897").map { marketConfig ->

            val contract = PendleMarketContract(
                blockchainGateway = getBlockchainGateway(),
                address = marketConfig.market
            )

            val tokens = contract.readTokens()

            val yt = getToken(tokens.yt)
            val pt = getToken(tokens.pt)
            val sy = getToken(tokens.sy)

            create(
                tokens = listOf(
                    pt, sy
                ),
                breakdown = refreshable {
                    listOf(
                        PoolingMarketTokenShare(
                            pt, erC20Resource.getBalance(getNetwork(), pt.address, contract.address)
                        ),
                        PoolingMarketTokenShare(
                            sy, erC20Resource.getBalance(getNetwork(), sy.address, contract.address)
                        )
                    )
                },
                positionFetcher = defaultPositionFetcher(contract.address),
                name = contract.readName(),
                identifier = marketConfig.market,
                address = contract.address,
                symbol = "PENDLE-LPT",
                totalSupply = refreshable {
                    contract.readTotalSupply()
                }.map {
                    it.asEth()
                }
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.PENDLE
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}