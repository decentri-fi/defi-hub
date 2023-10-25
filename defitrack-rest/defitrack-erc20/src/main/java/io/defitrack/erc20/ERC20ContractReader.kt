package io.defitrack.erc20

import arrow.core.Either.Companion.catch
import arrow.core.Option
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.multicall.MultiCallResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ERC20ContractReader(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getERC20(network: Network, address: String): Option<ERC20> {
        return catch {
            val correctAddress =
                if (address == "0x0" || address == "0x0000000000000000000000000000000000000000") ERC20Repository.NATIVE_WRAP_MAPPING[network]!! else address
            val contract = ERC20Contract(
                blockchainGatewayProvider.getGateway(network),
                correctAddress
            )
            val result = contract.readAsMulticall()
            ERC20(
                name = getValue(result.name, contract::readName),
                symbol = getValue(result.symbol, contract::readSymbol),
                decimals = getValue<BigInteger>(result.decimals, contract::readDecimals).toInt(),
                network = network,
                address = correctAddress.lowercase(),
                totalSupply = Refreshable.refreshable(
                    getValue<BigInteger>(result.totalSupply) {
                        contract.readTotalSupply()
                    }
                ) {
                    contract.readTotalSupply()
                },
            )
        }.mapLeft {
            logger.error("Error getting ERC20 contract for $address on $network", it)
        }.getOrNone()
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