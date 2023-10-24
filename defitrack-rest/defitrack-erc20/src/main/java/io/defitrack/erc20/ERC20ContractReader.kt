package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.multicall.MultiCallResult
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ERC20ContractReader(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val erc20Buffer = Cache.Builder<String, ERC20Contract>().build()
    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getERC20(network: Network, address: String): ERC20 {
        try {
            val correctAddress =
                if (address == "0x0" || address == "0x0000000000000000000000000000000000000000") ERC20Repository.NATIVE_WRAP_MAPPING[network]!! else address
            val key = network.name + "-" + address.lowercase()
            return erc20Buffer.get(key) {
                ERC20Contract(
                    blockchainGatewayProvider.getGateway(network),
                    correctAddress
                )
            }.let {
                val result = it.readAsMulticall()
                ERC20(
                    name = getValue(result.name, it::readName),
                    symbol = getValue(result.symbol, it::readSymbol),
                    decimals = getValue(result.decimals, it::decimals),
                    network = network,
                    address = correctAddress.lowercase(),
                    totalSupply = getValue(result.totalSupply, it::totalSupply),
                )
            }
        } catch (ex: Exception) {
            logger.error("Unable to fetch erc20 info", ex)
            throw ex
        }
    }

    private suspend inline fun <reified T> getValue(
        result: MultiCallResult,
        default: suspend () -> T
    ): T {
        return if (result.success) {
            result.data.first().value as T
        } else {
            default()
        }
    }

    suspend fun getBalance(network: Network, address: String, userAddress: String) = ERC20Contract(
        blockchainGatewayProvider.getGateway(network),
        address
    ).balanceOf(userAddress)

    suspend fun getAllowance(network: Network, address: String, userAddress: String, spenderAddress: String) =
        ERC20Contract(
            blockchainGatewayProvider.getGateway(network),
            address
        ).readAllowance(userAddress, spenderAddress)
}