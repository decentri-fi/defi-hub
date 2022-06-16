package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

abstract class DefaultLendingPositionService(
    val lendingMarketService: LendingMarketService,
    val gateway: BlockchainGatewayProvider,
) : LendingPositionService {

    override suspend fun getLendings(address: String): List<LendingPosition> {

        val markets = lendingMarketService.fetchLendingMarkets().filter {
            it.balanceFetcher != null
        }

        return gateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.balanceFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val balance = market.balanceFetcher!!.extractBalance(retVal)

            if (balance > BigInteger.ZERO) {
                LendingPosition(
                    market = market,
                    amount = balance,
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getLending(address: String, marketId: String): LendingPosition? = runBlocking {
        getLendings(address).firstOrNull {
            it.market.id == marketId
        }
    }

    override fun getProtocol(): Protocol {
        return lendingMarketService.getProtocol()
    }

    override fun getNetwork(): Network {
        return lendingMarketService.getNetwork()
    }
}