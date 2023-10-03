package io.defitrack.protocol.set.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
@ConditionalOnCompany(Company.SET)
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
                val token = getToken(it)
                val positions = tokenContract.getPositions()

                create(
                    identifier = it,
                    address = it,
                    name = token.name,
                    symbol = token.symbol,
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
                    marketSize = refreshable {
                        getPrice(positions).times(
                            tokenContract.totalSupply().asEth(tokenContract.decimals())
                        )
                    },
                    positionFetcher = defaultPositionFetcher(it),
                    investmentPreparer = null,
                    totalSupply = refreshable(token.totalDecimalSupply()) {
                        getToken(it).totalDecimalSupply()
                    })
            } catch (ex: Exception) {
                logger.error("Unable to import set with address $it")
                null
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    suspend fun getPrice(positions: List<SetTokenContract.Position>): BigDecimal {
        val price = positions.sumOf {
            val token = getToken(it.token)

            getPriceResource().calculatePrice(
                PriceRequest(
                    it.token,
                    getNetwork(),
                    it.amount.dividePrecisely(BigDecimal.TEN.pow(token.decimals)),
                    token.type
                )
            )
        }.toBigDecimal()

        return if (price <= BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else price
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}