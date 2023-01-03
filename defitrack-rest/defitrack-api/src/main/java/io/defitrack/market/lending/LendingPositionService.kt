package io.defitrack.market.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired

abstract class LendingPositionService : ProtocolService {

    @Autowired
    lateinit var erC20Resource: ERC20Resource

    @Autowired
    lateinit var blockchainGatewayProvider: BlockchainGatewayProvider
    abstract suspend fun getLendings(address: String): List<LendingPosition>

    open fun getLending(address: String, marketId: String): LendingPosition? = runBlocking {
        getLendings(address).firstOrNull {
            it.market.id == marketId
        }
    }
}