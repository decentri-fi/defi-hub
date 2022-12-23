package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
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