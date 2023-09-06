package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class PolygonBalanceService(
    erc20Resource: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BalanceService(blockchainGatewayProvider, erc20Resource) {

    override fun getNetwork(): Network = Network.POLYGON

    override fun nativeTokenName(): String {
        return "MATIC"
    }
}