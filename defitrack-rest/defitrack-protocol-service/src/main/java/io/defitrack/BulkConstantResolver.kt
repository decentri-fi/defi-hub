package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.multicall.MultiCallElement
import io.defitrack.evm.multicall.MultiCallResult
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Function

@Component
class BulkConstantResolver(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    suspend fun resolve(contracts: List<EvmContract>) {
        val evmContracts = contracts.groupBy {
            it.blockchainGateway.network
        }

        evmContracts.forEach { (network, contracts) ->
            resolveForNetwork(contracts, network)
        }
    }

    private suspend fun resolveForNetwork(contracts: List<EvmContract>, network: Network) {
        val gateway = blockchainGatewayProvider.getGateway(network)
        val functionByContracts = contracts.flatMap { contract ->
            contract.constantFunctions.map { function ->
                function to contract
            }
        }

        val multicallResults = gateway.readMultiCall(
            functionByContracts.map {
                MultiCallElement(
                    it.first,
                    it.second.address
                )
            }
        )

        val s = multicallResults.mapIndexed { index, result ->
            val pair = functionByContracts[index]
            ConstantResult(
                pair.first,
                result,
                pair.second
            )
        }.groupBy {
            it.contract
        }.forEach {
            it.key.resolveConstants(
                it.value.associate { constantResult ->
                    constantResult.function to constantResult.result
                }
            )
        }
    }

    data class ConstantResult(
        val function: Function,
        val result: MultiCallResult,
        val contract: EvmContract
    )

}