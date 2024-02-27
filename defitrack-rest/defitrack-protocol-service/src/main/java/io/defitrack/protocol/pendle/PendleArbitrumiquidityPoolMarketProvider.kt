package io.defitrack.protocol.pendle

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import java.util.concurrent.Flow

@ConditionalOnNetwork(Network.ARBITRUM)
@ConditionalOnCompany(Company.PENDLE)
@Component
class PendleArbitrumiquidityPoolMarketProvider : PoolingMarketProvider() {

    //TODO: create for ethereum
    override suspend fun produceMarkets() = channelFlow<PoolingMarket> {

        val factory = PendleMarketFactoryContract(
            getBlockchainGateway(),
            "0x2FCb47B58350cD377f94d3821e7373Df60bD9Ced"
        )

        factory.getMarkets("154873897").map { marketConfig ->

            val contract = PendleMarketContract(
                blockchainGateway = getBlockchainGateway(),
                address = marketConfig.market
            )

            val tokens = contract.readTokens()

            val yt = getToken(tokens.yt)
            val pt = getToken(tokens.pt)
            val sy = getToken(tokens.sy)

            val syContract = PendleSyContract(
                blockchainGateway = getBlockchainGateway(),
                address = sy.address
            )

            val assetAddress = syContract.yieldToken()
            val asset = erC20Resource.getTokenInformation(getNetworkForToken(assetAddress), assetAddress)

            send(
                createLPMarket(pt, sy, contract, asset, marketConfig)
            )

            send(
                create(
                    tokens = listOf(sy),
                    breakdown = refreshable {
                        listOf(
                            PoolingMarketTokenShare(
                                sy, erC20Resource.getBalance(getNetwork(), sy.address, contract.address)
                            )
                        )
                    },
                    name = "YT " + asset.name,
                    address = yt.address,
                    positionFetcher = defaultPositionFetcher(yt.address),
                    identifier = yt.address,
                    symbol = yt.symbol,
                    totalSupply = yt.totalDecimalSupply().toRefreshable(),
                )
            )
        }
    }

    private suspend fun createLPMarket(
        pt: FungibleTokenInformation,
        sy: FungibleTokenInformation,
        contract: PendleMarketContract,
        asset: FungibleTokenInformation,
        marketConfig: PendleMarketFactoryContract.Market
    ) = create(
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
        name = asset.name + " Pool",
        identifier = marketConfig.market,
        address = contract.address,
        symbol = "PENDLE-LPT",
        totalSupply = refreshable {
            contract.readTotalSupply()
        }.map {
            it.asEth()
        }
    )

    fun getNetworkForToken(address: String): Network {
        //weird things at pendle, jesus
        return if (address == "0xae7ab96520de3a18e5e111b5eaab095312d7fe84" || address == "0x35fa164735182de50811e8e2e824cfb9b6118ac2") {
            Network.ETHEREUM
        } else {
            getNetwork()
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.PENDLE
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}