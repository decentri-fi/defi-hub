package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.output.ERC20Client
import org.springframework.stereotype.Service

@Service
class PolygonBalanceService(
    erc20Resource: ERC20Client,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BalanceService(blockchainGatewayProvider, erc20Resource) {

    override fun getNetwork(): Network = Network.POLYGON

    override fun nativeTokenName(): String {
        return "MATIC"
    }
}