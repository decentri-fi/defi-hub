package io.defitrack.balance.service.l2

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Service

@Service
class OptimismBalanceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erc20Resource: ERC20Resource,
) : BalanceService(blockchainGatewayProvider, erc20Resource) {

    override fun getNetwork(): Network = Network.OPTIMISM

    override fun nativeTokenName(): String {
        return "ETH"
    }
}