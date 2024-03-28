package io.defitrack.protocol.application.set

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
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
@ConditionalOnNetwork(Network.ETHEREUM)
class EthereumSetPoolingMarketProvider(
    private val ethereumSetProvider: EthereumSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return ethereumSetProvider.getSets().mapNotNull { set ->
            try {
                val tokenContract = createContract { SetTokenContract(set) }
                val token = getToken(set)

                val breakdown = refreshable {
                    tokenContract.getPositions().map {
                        val supply = tokenContract.totalSupply().get().asEth(tokenContract.readDecimals())
                        val underlying = getToken(it.token)
                        val reserve = it.amount.toBigDecimal()
                            .times(tokenContract.getPositionMultiplier().asEth())
                            .times(supply).toBigInteger()
                        PoolingMarketTokenShare(
                            token = underlying,
                            reserve = reserve,
                        )
                    }
                }
                create(
                    identifier = set,
                    address = set,
                    name = token.name,
                    symbol = token.symbol,
                    breakdown = breakdown,
                    positionFetcher = defaultPositionFetcher(set),
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

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}