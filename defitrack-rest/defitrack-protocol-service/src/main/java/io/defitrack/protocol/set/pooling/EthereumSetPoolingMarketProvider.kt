package io.defitrack.protocol.set.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.SetTokenContract
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
@ConditionalOnCompany(Company.SET)
class EthereumSetPoolingMarketProvider(
    private val ethereumSetProvider: EthereumSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return ethereumSetProvider.getSets().mapNotNull { set ->
            try {
                val tokenContract = SetTokenContract(
                    getBlockchainGateway(), set
                )

                val token = getToken(set)

                val positionsRefreshable = refreshable {
                    tokenContract.getPositions()
                }


                val breakdown = positionsRefreshable.map { positions ->
                    positions.map {
                        val supply = tokenContract.totalSupply().get().asEth(tokenContract.readDecimals())
                        val underlying = getToken(it.token)
                        val reserve = it.amount.toBigDecimal()
                            .times(tokenContract.positionMultiplier.await().asEth())
                            .times(supply).toBigInteger()
                        PoolingMarketTokenShare(
                            token = underlying,
                            reserve = reserve,
                            reserveUSD = getPrice(it.token, reserve.asEth(underlying.decimals))
                        )
                    }
                }
                create(
                    identifier = set,
                    address = set,
                    name = token.name,
                    symbol = token.symbol,
                    tokens = positionsRefreshable.get().map { position -> getToken(position.token) },
                    breakdown = breakdown,
                    apr = null,
                    marketSize = tokenContract.totalSupply().map {
                        it.asEth(token.decimals).times(
                            getPrice(tokenContract.getPositions())
                        )
                    },
                    positionFetcher = defaultPositionFetcher(set),
                    investmentPreparer = null,
                    totalSupply = refreshable(token.totalDecimalSupply()) {
                        getToken(set).totalDecimalSupply()
                    })
            } catch (ex: Exception) {
                logger.error("Unable to import set with address $set")
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
                GetPriceCommand(
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