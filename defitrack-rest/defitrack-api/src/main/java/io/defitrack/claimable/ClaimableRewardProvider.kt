package io.defitrack.claimable

import io.defitrack.abi.ABIResource
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ProtocolProvider
import io.defitrack.protocol.ProtocolService
import org.springframework.beans.factory.annotation.Autowired

abstract class ClaimableRewardProvider : ProtocolService {

    @Autowired
    private lateinit var protocolProvider: ProtocolProvider;

    @Autowired
    lateinit var abiResource: ABIResource;

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    abstract suspend fun claimables(address: String): List<Claimable>

    override fun getProtocol(): Protocol {
        return protocolProvider.getProtocol()
    }

    fun getBlockchainGateway(): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(getNetwork())
    }
}