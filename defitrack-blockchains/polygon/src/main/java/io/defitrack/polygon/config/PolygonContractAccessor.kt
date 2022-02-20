package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import org.springframework.stereotype.Component

@Component
class PolygonContractAccessor(abiDecoder: AbiDecoder, val polygonGateway: PolygonGateway) :
    EvmContractAccessor(abiDecoder) {

    override fun getMulticallContract(): String {
        return "0x11ce4B23bD875D7F5C6a31084f55fDe1e9A87507"
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun getGateway(): EvmGateway {
        return polygonGateway
    }
}