package io.defitrack.pool

import io.defitrack.pool.contract.LPTokenContract
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import org.springframework.stereotype.Service
import java.util.*

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
        val key = "${network.name}-${address.lowercase(Locale.getDefault())}"
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