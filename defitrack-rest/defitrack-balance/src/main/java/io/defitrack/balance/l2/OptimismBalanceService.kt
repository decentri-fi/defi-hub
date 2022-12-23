package io.defitrack.balance.l2

import io.defitrack.balance.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
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