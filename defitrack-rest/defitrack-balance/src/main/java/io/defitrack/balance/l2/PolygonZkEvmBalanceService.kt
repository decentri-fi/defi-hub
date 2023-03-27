package io.defitrack.balance.l2

import io.defitrack.balance.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
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