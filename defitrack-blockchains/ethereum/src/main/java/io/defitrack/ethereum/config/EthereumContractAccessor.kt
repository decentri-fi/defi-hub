package io.defitrack.ethereum.config


import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.stereotype.Component

@Component
class EthereumContractAccessor(abiDecoder: AbiDecoder, val ethereumGateway: EthereumGateway) :
    EvmContractAccessor(abiDecoder) {
    override fun getMulticallContract(): String {
        return "0xeefba1e63905ef1d7acba5a8513c70307c1ce441"
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    override fun getGateway(): EvmGateway {
        return ethereumGateway
    }
}