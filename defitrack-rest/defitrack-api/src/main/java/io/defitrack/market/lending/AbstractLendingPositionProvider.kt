package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

abstract class AbstractLendingPositionProvider(
    val lendingMarketProvider: LendingMarketProvider,
    val gateway: BlockchainGatewayProvider,
) : LendingPositionProvider() {

    override suspend fun getLendings(address: String): List<LendingPosition> {

        val markets = lendingMarketProvider.getMarkets().filter {
            it.positionFetcher != null
        }

        return gateway.getGateway(lendingMarketProvider.getNetwork()).readMultiCall(
            markets.map {
                it.positionFetcher!!.toMulticall(address)
            }
        ).mapIndexed { index, retVal ->
            val market = markets[index]
            val position = market.positionFetcher!!.extractBalance(retVal)

            if (position.underlyingAmount > BigInteger.ZERO) {
                LendingPosition(
                    market = market,
                    underlyingAmount = position.underlyingAmount,
                    tokenAmount = position.tokenAmount
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
}