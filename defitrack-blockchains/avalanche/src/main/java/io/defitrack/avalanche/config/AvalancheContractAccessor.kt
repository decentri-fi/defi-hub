package io.defitrack.avalanche.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.stereotype.Component

@Component
class AvalancheContractAccessor(abiDecoder: AbiDecoder, val avalancheGateway: AvalancheGateway) :
    EvmContractAccessor(abiDecoder) {
    override fun getMulticallContract(): String {
        return "0x6FfF95AC47b586bDDEea244b3c2fe9c4B07b9F76"
    }

    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }

    override fun getGateway(): EvmGateway {
        return avalancheGateway
    }
}