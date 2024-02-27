package io.defitrack.protocol.bancor.pool

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
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
            .parMapNotNull(concurrency = 8) { pool -> create(pool, bancor) }
            .forEach {
                send(it)
            }
    }

    private fun poolTokenContract(it: String) = with(getBlockchainGateway()) { PoolTokenContract(it) }

    private suspend fun create(
        pool: PoolTokenContract,
        bancor: BancorNetworkContract
    ) = try {
        val token = getToken(pool.address)
        val underlying = getToken(pool.reserveToken.await())
        create(
            identifier = pool.address,
            address = pool.address,
            name = token.name,
            symbol = token.symbol,
            tokens = listOf(underlying),
            investmentPreparer = BancorPoolInvestmentPreparer(
                getERC20Resource(), bancor, underlying.address
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

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}