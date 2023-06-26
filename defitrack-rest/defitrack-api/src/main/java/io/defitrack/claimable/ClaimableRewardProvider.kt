package io.defitrack.claimable

import io.defitrack.abi.ABIResource
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.CompanyProvider
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired

abstract class ClaimableRewardProvider : ProtocolService {

    @Autowired
    private lateinit var companyProvider: CompanyProvider;

    @Autowired
    lateinit var abiResource: ABIResource;

    @Autowired
    lateinit var erC20Resource: ERC20Resource;

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    abstract suspend fun claimables(address: String): List<Claimable>

    fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(getNetwork())
    }
}