package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

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

                try {
                    val result = it.readData()
                    ERC20(
                        name = if (result[0].success) result[0].data.first().value as String else it.name(),
                        symbol = if (result[1].success) result[1].data.first().value as String else it.symbol(),
                        decimals = if (result[2].success) (result[2].data.first().value as BigInteger).toInt() else it.decimals(),
                        network = network,
                        address = correctAddress.lowercase(),
                        totalSupply = if (result[3].success) result[3].data.first().value as BigInteger else it.totalSupply(),
                    )
                } catch (ex: Exception) {
                    logger.debug("Unable to do it in a single call for token ${address} on network ${network.name}")
                    ERC20(
                        name = it.name(),
                        symbol = it.symbol(),
                        decimals = it.decimals(),
                        network = network,
                        address = correctAddress.lowercase(),
                        totalSupply = it.totalSupply()
                    )
                }
            }
        } catch (ex: Exception) {
            logger.error("Unable to fetch erc20 info", ex)
            throw ex
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
        ).allowance(userAddress, spenderAddress)
}