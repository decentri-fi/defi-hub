package io.defitrack.balance.service.l1

import io.defitrack.balance.service.BalanceService
import io.defitrack.balance.service.dto.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class AvaxBalanceService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Service: ERC20Resource
) : BalanceService(blockchainGatewayProvider, erC20Service) {

    override suspend fun getTokenBalances(user: String): List<TokenBalance> {
        return emptyList()
    }

    override fun getNetwork(): Network = Network.AVALANCHE
    override fun nativeTokenName(): String {
        return "AVAX"
    }
}