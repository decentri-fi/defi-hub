package io.defitrack.set.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class EthereumSetPoolingMarketProvider(
    private val ethereumSetProvider: EthereumSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return ethereumSetProvider.getSets().mapNotNull {
            try {
                val tokenContract = SetTokenContract(
                    getBlockchainGateway(), it
                )

                val supply = tokenContract.totalSupply().asEth(tokenContract.decimals())


                val positions = tokenContract.getPositions()
                PoolingMarket(
                    id = "set-ethereum-$it",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it,
                    name = tokenContract.name(),
                    symbol = tokenContract.symbol(),
                    tokens = positions.map {
                        getToken(it.token).toFungibleToken()
                    },
                    breakdown = positions.map {
                        val underlying = getToken(it.token)
                        PoolingMarketTokenShare(
                            token = underlying.toFungibleToken(),
                            reserve = it.amount.toBigDecimal().times(supply).toBigInteger(),
                            reserveUSD = getPriceResource().calculatePrice(
                                PriceRequest(
                                    it.token,
                                    getNetwork(),
                                    it.amount.asEth(underlying.decimals)
                                )
                            ).toBigDecimal().times(supply)
                        )
                    },
                    apr = null,
                    marketSize = null,
                    tokenType = TokenType.SET,
                    positionFetcher = defaultPositionFetcher(it),
                    investmentPreparer = null,
                    totalSupply = tokenContract.totalSupply()
                )
            } catch (ex: Exception) {
                logger.error("Unable to import set with address $it")
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}