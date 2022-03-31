package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class FantomBalanceService(
    contractAccessorGateway: ContractAccessorGateway,
    erC20Service: ERC20Resource
) : BalanceService(contractAccessorGateway, erC20Service) {

    override fun getNetwork(): Network = Network.FANTOM

    override fun nativeTokenName(): String {
        return "FTM"
    }
}