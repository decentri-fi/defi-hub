package io.defitrack.erc20

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.EvmContractAccessor
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class ERC20Service(
    private val abiService: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway
) {

    val erc20Buffer = Cache.Builder().build<String, ERC20Contract>()
    val erc20ABI by lazy {
        abiService.getABI("general/ERC20.json")
    }

    fun getERC20(network: Network, address: String): ERC20 {
        val correctAddress =
            if (address == "0x0" || address == "0x0000000000000000000000000000000000000000") ERC20Repository.NATIVE_WRAP_MAPPING[network]!! else address
        val key = network.name + "-" + address.lowercase()
        return runBlocking(Dispatchers.IO) {
            erc20Buffer.get(key) {
                ERC20Contract(
                    contractAccessorGateway.getGateway(network),
                    erc20ABI,
                    correctAddress
                )
            }.let {
                ERC20(
                    name = it.name,
                    symbol = it.symbol,
                    decimals = it.decimals,
                    network = network,
                    address = correctAddress.lowercase()
                )
            }
        }
    }

    fun getBalance(network: Network, address: String, userAddress: String) = ERC20Contract(
        contractAccessorGateway.getGateway(network),
        erc20ABI,
        address
    ).balanceOf(userAddress)
}