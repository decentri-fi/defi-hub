package io.defitrack.lending

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

abstract class DefaultLendingUserService(
    val lendingMarketService: LendingMarketService,
    val gateway: ContractAccessorGateway,
) : LendingUserService {

    override suspend fun getLendings(address: String): List<LendingElement> {

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
                LendingElement(
                    id = "compound-ethereum-${market.address}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    name = market.name,
                    rate = market.rate,
                    amount = balance,
                    token = market.token
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getLending(address: String, vaultId: String): LendingElement? = runBlocking {
        getLendings(address).firstOrNull {
            it.id == vaultId
        }
    }

    override fun getProtocol(): Protocol {
        return lendingMarketService.getProtocol()
    }

    override fun getNetwork(): Network {
        return lendingMarketService.getNetwork()
    }
}