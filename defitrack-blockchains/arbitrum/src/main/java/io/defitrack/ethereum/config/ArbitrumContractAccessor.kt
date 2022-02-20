package io.defitrack.ethereum.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.stereotype.Component

@Component
class ArbitrumContractAccessor(abiDecoder: AbiDecoder, val arbitrumGateway: ArbitrumGateway) :
    EvmContractAccessor(abiDecoder) {

    override fun getMulticallContract(): String {
        return "0x2d7aca3bD909bc5DC6DC70894669Adfb6483Bf5F"
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }

    override fun getGateway(): EvmGateway {
        return arbitrumGateway
    }
}