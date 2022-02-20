package io.defitrack.optimism.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.stereotype.Component

@Component
class OptimismContractAccessor(abiDecoder: AbiDecoder, val optimismGateway: OptimismGateway) :
    EvmContractAccessor(abiDecoder) {
    override fun getMulticallContract(): String {
        return "0xaFE0A0302134df664f0EE212609CA8Fb89255BE4"
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }

    override fun getGateway(): EvmGateway {
        return optimismGateway
    }
}