package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class PolygonBalanceService(
    erC20Service: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BalanceService(blockchainGatewayProvider, erC20Service) {

    override fun getNetwork(): Network = Network.POLYGON

    override fun nativeTokenName(): String {
        return "MATIC"
    }
}