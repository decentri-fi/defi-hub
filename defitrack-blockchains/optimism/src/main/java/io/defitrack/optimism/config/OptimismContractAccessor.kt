package io.defitrack.optimism.config

import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.websocket.WebSocketClient
import org.web3j.protocol.websocket.WebSocketService
import java.net.URI

@Component
class OptimismContractAccessor(abiDecoder: AbiDecoder, val arbitrumGateway: OptimismGateway) :
    EvmContractAccessor(abiDecoder) {
    override fun getMulticallContract(): String {
        return "0xaFE0A0302134df664f0EE212609CA8Fb89255BE4"
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }

    override fun executeCall(from: String?, address: String, function: Function, endpoint: String?): List<Type<*>> {
        val encodedFunction = FunctionEncoder.encode(function)
        val ethCall = call(from, address, encodedFunction, endpoint)
        return FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
    }

    private fun call(
        from: String? = "0x0000000000000000000000000000000000000000",
        contract: String,
        encodedFunction: String,
        endpoint: String?
    ): EthCall {
        val web3j = endpoint?.let {
            constructEndpoint(it)
        } ?: arbitrumGateway.web3j()

        return web3j.ethCall(
            Transaction.createEthCallTransaction(
                from,
                contract,
                encodedFunction
            ), DefaultBlockParameterName.LATEST
        ).send()
    }

    private fun constructEndpoint(endpoint: String): Web3j {
        return if (endpoint.startsWith("ws")) {
            val webSocketClient = WebSocketClient(URI.create(endpoint))
            val webSocketService = WebSocketService(webSocketClient, false)
            webSocketService.connect()
            Web3j.build(webSocketService)
        } else {
            Web3j.build(HttpService(endpoint, false))
        }
    }
}