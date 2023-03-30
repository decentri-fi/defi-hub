package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class FantomBalanceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Service: ERC20Resource
) : BalanceService(blockchainGatewayProvider, erC20Service) {

    override fun getNetwork(): Network = Network.FANTOM

    override fun nativeTokenName(): String {
        return "FTM"
    }
}