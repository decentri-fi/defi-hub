package io.defitrack.polygonmumbai.config

import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.abi.domain.AbiContractEvent
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.reactivex.Flowable
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketClient
import org.web3j.protocol.websocket.WebSocketService
import java.net.URI

@Component
class PolygonMumbaiContractAccessor(abiDecoder: AbiDecoder, val polygonMumbaiGateway: PolygonMumbaiGateway) :
    EvmContractAccessor(abiDecoder) {

    override fun getMulticallContract(): String {
        return "0xb976685f95681bb0bd9af04bb2a381d90ac23704"
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun getGateway(): EvmGateway {
        return polygonMumbaiGateway
    }
}