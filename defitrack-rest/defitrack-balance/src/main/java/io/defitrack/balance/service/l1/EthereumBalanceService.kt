package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.port.output.ERC20Client
import org.springframework.stereotype.Service

@Service
class EthereumBalanceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erc20Resource: ERC20Client,
) : BalanceService(blockchainGatewayProvider, erc20Resource) {
    override fun getNetwork(): Network = Network.ETHEREUM

    override fun nativeTokenName(): String {
        return "ETH"
    }
}