package io.defitrack.protocol.application.pendle

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.domain.asShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pendle.PendleMarketContract
import io.defitrack.protocol.pendle.PendleMarketFactoryContract
import io.defitrack.protocol.pendle.PendleSyContract
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

abstract class PendleLiquidityPoolMarketProvider(
    private val marketFactory: String,
    private val startBlock: String,
    private val network: Network
) : PoolingMarketProvider() {

    override suspend fun produceMarkets() = channelFlow {

        val factory = createContract {
            PendleMarketFactoryContract(marketFactory)
        }

        factory.getMarkets(startBlock).map { marketConfig ->

            val contract = with(getBlockchainGateway()) {
                PendleMarketContract(
                    address = marketConfig.market
                )
            }

            val tokens = contract.readTokens()

            val yt = getToken(tokens.yt)
            val pt = getToken(tokens.pt)
            val sy = getToken(tokens.sy)

            val syContract = with(getBlockchainGateway()) { PendleSyContract(address = sy.address) }

            val assetAddress = syContract.yieldToken()
            val asset = erC20Resource.getTokenInformation(getNetworkForToken(assetAddress), assetAddress)

            send(
                createLPMarket(pt, sy, contract, asset, marketConfig)
            )

            send(
                create(
                    breakdown = refreshable {
                        sy.asShare(getBalance(sy.address, contract.address)).nel()
                    },
                    name = "YT " + asset.name,
                    address = yt.address,
                    positionFetcher = defaultPositionFetcher(yt.address),
                    identifier = yt.address,
                    symbol = yt.symbol,
                    totalSupply = refreshable(yt.totalDecimalSupply()) {
                        getToken(tokens.yt).totalDecimalSupply()
                    },
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
        breakdown = refreshable {
            listOf(
                pt.asShare(getBalance(pt.address, contract.address)),
                sy.asShare(getBalance(sy.address, contract.address))
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
        return network
    }
}