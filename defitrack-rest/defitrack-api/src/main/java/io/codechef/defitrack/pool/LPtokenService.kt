package io.codechef.defitrack.pool

import io.codechef.defitrack.pool.contract.LPTokenContract
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import org.springframework.stereotype.Service

@Service
class LPtokenService(
    private val abiService: ABIResource,
    private val contractAccessors: List<EvmContractAccessor>
) {

    private val lpBuffer = mutableMapOf<String, LPTokenContract>()
    val lpABI by lazy {
        abiService.getABI("uniswap/UniswapV2Pair.json")
    }


    fun getLP(network: Network, address: String): LPTokenContract {
        val key = "${network.name}-${address.toLowerCase()}"
        return lpBuffer.getOrPut(key) {
            LPTokenContract(
                getContractAccessor(network),
                lpABI,
                address = address
            )
        }
    }

    fun getContractAccessor(network: Network): EvmContractAccessor {
        return contractAccessors.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("$network not supported")
    }
}