package io.defitrack.market.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired

abstract class LendingPositionProvider {

    @Autowired
    lateinit var erC20Resource: ERC20Resource

    @Autowired
    lateinit var blockchainGatewayProvider: BlockchainGatewayProvider
    abstract suspend fun getPositions(protocol: String, address: String): List<LendingPosition>
}