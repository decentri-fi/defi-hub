package io.defitrack.polygon.config

import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.ContractInteractionCommand
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.web3j.EvmGateway
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.EthCall

@Component
class PolygonContractAccessor(
    abiDecoder: AbiDecoder,
    private val polygonGateway: PolygonGateway,
    private val httpClient: HttpClient,
) :
    EvmContractAccessor(abiDecoder) {

    override fun call(
        from: String?,
        contract: String,
        encodedFunction: String
    ): EthCall = runBlocking(Dispatchers.IO) {
        httpClient.post("http://defitrack-polygon:8080/contract") {
            this.body = ContractInteractionCommand(
                from = from,
                contract = contract,
                function = encodedFunction
            )
        }
    }

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