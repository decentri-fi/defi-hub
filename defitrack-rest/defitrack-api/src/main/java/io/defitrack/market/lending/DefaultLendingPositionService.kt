package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

abstract class DefaultLendingPositionService(
    val lendingMarketProvider: LendingMarketProvider,
    val gateway: BlockchainGatewayProvider,
) : LendingPositionService() {

    override suspend fun getLendings(address: String): List<LendingPosition> {

        val markets = lendingMarketProvider.getMarkets().filter {
            it.positionFetcher != null
        }

        return gateway.getGateway(getNetwork()).readMultiCall(
            markets.map {
                it.positionFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val balance = market.positionFetcher!!.extractBalance(retVal)

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
        return lendingMarketProvider.getProtocol()
    }

    override fun getNetwork(): Network {
        return lendingMarketProvider.getNetwork()
    }
}