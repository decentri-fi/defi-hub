package io.defitrack.balance.service.l2

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.output.ERC20Client
import org.springframework.stereotype.Service

@Service
class PolygonZkEvmBalanceService(
    erC20Service: ERC20Client,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BalanceService(blockchainGatewayProvider, erC20Service) {

    override fun getNetwork(): Network = Network.POLYGON_ZKEVM

    override fun nativeTokenName(): String {
        return "ETH"
    }
}