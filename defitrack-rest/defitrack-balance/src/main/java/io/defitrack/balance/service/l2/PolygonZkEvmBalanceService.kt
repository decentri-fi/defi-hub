package io.defitrack.balance.service.l2

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Service

@Service
class PolygonZkEvmBalanceService(
    erC20Service: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
) : BalanceService(blockchainGatewayProvider, erC20Service) {

    override fun getNetwork(): Network = Network.POLYGON_ZKEVM

    override fun nativeTokenName(): String {
        return "ETH"
    }
}