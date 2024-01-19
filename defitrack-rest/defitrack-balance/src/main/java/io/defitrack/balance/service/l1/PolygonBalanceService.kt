package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
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