package io.defitrack.erc20

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ERC20Service(
    private val abiService: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val erc20Buffer = Cache.Builder().build<String, ERC20Contract>()
    val erc20ABI by lazy {
        runBlocking {
            abiService.getABI("general/ERC20.json")
        }
    }

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getERC20(network: Network, address: String): ERC20 {
        try {
            val correctAddress =
                if (address == "0x0" || address == "0x0000000000000000000000000000000000000000") ERC20Repository.NATIVE_WRAP_MAPPING[network]!! else address
            val key = network.name + "-" + address.lowercase()
            return erc20Buffer.get(key) {
                ERC20Contract(
                    blockchainGatewayProvider.getGateway(network),
                    erc20ABI,
                    correctAddress
                )
            }.let {
                ERC20(
                    name = it.name(),
                    symbol = it.symbol(),
                    decimals = it.decimals(),
                    network = network,
                    address = correctAddress.lowercase(),
                    totalSupply = it.totalSupply()
                )
            }
        } catch (ex: Exception) {
            logger.error("Unable to fetch erc20 info", ex)
            ex.printStackTrace()
            throw ex
        }
    }

    suspend fun getBalance(network: Network, address: String, userAddress: String) = ERC20Contract(
        blockchainGatewayProvider.getGateway(network),
        erc20ABI,
        address
    ).balanceOf(userAddress)
}