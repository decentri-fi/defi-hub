package io.defitrack.claimable

import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired

@Deprecated("use io.defitrack.claimable.ClaimableMarketProvider instead")
abstract class AbstractUserClaimableProvider : ProtocolService {

    @Autowired
    lateinit var erC20Resource: ERC20Resource

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    abstract suspend fun claimables(address: String): List<UserClaimable>

    fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(getNetwork())
    }
}