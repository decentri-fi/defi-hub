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

@ConditionalOnNetwork(Network.ETHEREUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleLiquitityPoolMarketProvider : PoolingMarketProvider() {

    val liquidityMarkets = listOf(
        "0xf32e58f92e60f4b0a37a69b95d642a471365eae8"
    )

    override suspend fun fetchMarkets(): List<PoolingMarket> {

        val factory = PendleMarketFactoryContract(
            getBlockchainGateway(),
            "0x1A6fCc85557BC4fB7B534ed835a03EF056552D52"
        )

        return factory.getMarkets().map { marketConfig ->

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
                positionFetcher = PositionFetcher(contract::activeBalanceFn),
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
        return Network.ETHEREUM
    }
}