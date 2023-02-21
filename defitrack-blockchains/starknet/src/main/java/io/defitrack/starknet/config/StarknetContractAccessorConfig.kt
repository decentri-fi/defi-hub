package io.defitrack.starknet.config

import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.limitAttempts
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.retry
import com.swmansion.starknet.data.selectorFromName
import com.swmansion.starknet.data.types.Felt
import io.defitrack.common.network.Network
import io.defitrack.evm.abi.AbiDecoder
import io.defitrack.evm.contract.BlockchainGateway
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.methods.response.EthCall
import java.math.BigInteger

@Configuration
class StarknetContractAccessorConfig(
    private val abiDecoder: AbiDecoder,
    private val httpClient: HttpClient,
    @Value("\${io.defitrack.services.starknet.endpoint:http://defitrack-starknet:8080}") private val endpoint: String,
) {

    val DEFAULT_ENTRY_POINT_NAME = "__default__"
    val DEFAULT_L1_ENTRY_POINT_NAME = "__l1_default__"
    val DEFAULT_ENTRY_POINT_SELECTOR = 0
    val EXECUTE_ENTRY_POINT_NAME = "__execute__"
    private val MASK_250 = BigInteger.valueOf(2).pow(250) - BigInteger.ONE


    @Bean
    fun starknetContractAccessor(): BlockchainGateway {
        return object : BlockchainGateway(
            abiDecoder,
            Network.ARBITRUM,
            "0x2d7aca3bD909bc5DC6DC70894669Adfb6483Bf5F",
            httpClient,
            endpoint
        ) {

            override suspend fun executeCall(
                address: String,
                function: org.web3j.abi.datatypes.Function,
            ): List<Type<*>> {
                val ethCall = doCall(
                    address,
                    selectorFromName(function.name),
                    function.inputParameters.map {
                        TypeEncoder.encode(it)
                    }
                )
                return FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
            }

            suspend fun doCall(
                contract: String,
                entryPointSelector: Felt,
                calldata: List<String>
            ): EthCall = withContext(Dispatchers.IO) {
                retry(limitAttempts(5) + binaryExponentialBackoff(1000, 10000)) {
                    httpClient.post("$endpoint/starknet-contract/call") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            StarknetInteractionCommand(
                                contract,
                                entryPointSelector.hexString(),
                                calldata
                            )
                        )
                    }.body()
                }
            }

        }
    }
}