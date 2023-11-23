package io.defitrack

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.MultiCallResult
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Function

@Component
class BulkConstantResolver(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    suspend fun <T : EvmContract> resolve(contracts: List<T>): List<T> {
        val evmContracts = contracts.groupBy {
            it.blockchainGateway.network
        }

        evmContracts.forEach { (network, contracts) ->
            resolveForNetwork(contracts, network)
        }

        return contracts
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
                it.first
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
        val function: ContractCall,
        val result: MultiCallResult,
        val contract: EvmContract
    )

}