package io.defitrack.protocol.application.bancor

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.bancor.BancorEthereumProvider
import io.defitrack.protocol.bancor.contract.BancorNetworkContract
import io.defitrack.protocol.bancor.contract.PoolTokenContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BANCOR)
class BancorEthereumPoolingMarketProvider(
    private val bancorEthreumProvider: BancorEthereumProvider,
) : PoolingMarketProvider() {

    override fun getProtocol(): Protocol {
        return Protocol.BANCOR
    }

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val bancor = BancorNetworkContract(
            getBlockchainGateway(), bancorEthreumProvider.bancorNetwork
        )

        bancor.liquidityPools()
            .map(::poolTokenContract)
            .resolve()
            .parMapNotNull(concurrency = 8) { pool -> createMarket(pool, bancor) }
            .forEach {
                send(it)
            }
    }

    private fun poolTokenContract(it: String) = with(getBlockchainGateway()) { PoolTokenContract(it) }

    private suspend fun createMarket(
        pool: PoolTokenContract,
        bancor: BancorNetworkContract
    ): PoolingMarket? {
        return try {
            val token = getToken(pool.address)
            val underlying = getToken(pool.reserveToken.await())

            create(
                identifier = pool.address,
                breakdown = refreshable { emptyList() },
                address = pool.address,
                name = token.name,
                symbol = token.symbol,
                investmentPreparer = BancorPoolInvestmentPreparer(
                    getERC20Resource(), balanceResource, bancor, underlying.address
                ),
                positionFetcher = defaultPositionFetcher(token.address),
                totalSupply = refreshable {
                    getToken(pool.address).totalDecimalSupply()
                }
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}