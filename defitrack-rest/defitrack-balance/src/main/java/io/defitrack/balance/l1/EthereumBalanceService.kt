package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class EthereumBalanceService(
    contractAccessorGateway: ContractAccessorGateway,
    erc20Resource: ERC20Resource,
) : BalanceService(contractAccessorGateway, erc20Resource) {
    override fun getNetwork(): Network = Network.ETHEREUM

    override fun nativeTokenName(): String {
        return "ETH"
    }
}