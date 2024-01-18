package io.defitrack.erc20.adapter.contract

import arrow.core.Either.Companion.catch
import arrow.core.Option
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.application.repository.ERC20Repository
import io.defitrack.erc20.application.repository.NATIVE_WRAP_MAPPING
import io.defitrack.erc20.port.output.ReadAllowancePort
import io.defitrack.erc20.port.output.ReadBalancePort
import io.defitrack.erc20.port.output.ReadERC20Port
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.MultiCallResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Component
private class ERC20ContractReader(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : ReadERC20Port, ReadBalancePort, ReadAllowancePort {

    val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getERC20(network: Network, address: String): Option<ERC20> {
        return catch {
            val correctAddress =
                if (address == "0x0" || address == "0x0000000000000000000000000000000000000000") NATIVE_WRAP_MAPPING[network]!! else address
            val contract = ERC20Contract(
                blockchainGatewayProvider.getGateway(network),
                correctAddress
            )
            val result = contract.fetchERC20Information()
            ERC20(
                name = getValue(result.name, contract::readName),
                symbol = getValue(result.symbol, contract::readSymbol),
                decimals = getValue<BigInteger>(result.decimals, contract::readDecimals).toInt(),
                network = network,
                address = correctAddress.lowercase(),
                totalSupply = refreshable(
                    getValue<BigInteger>(result.totalSupply) {
                        contract.readTotalSupply()
                    }
                ) {
                    contract.readTotalSupply()
                },
            )
        }.mapLeft {
            logger.error("Error getting ERC20 contract for $address on ${network.slug}: ", it.message)
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

    override suspend fun getBalance(network: Network, address: String, userAddress: String) = ERC20Contract(
        blockchainGatewayProvider.getGateway(network),
        address
    ).balanceOf(userAddress)

    override suspend fun getNativeBalance(network: Network, userAddress: String): BigDecimal {
        return blockchainGatewayProvider.getGateway(network).getNativeBalance(userAddress)
    }

    override suspend fun getAllowance(network: Network, address: String, userAddress: String, spenderAddress: String) =
        ERC20Contract(
            blockchainGatewayProvider.getGateway(network),
            address
        ).readAllowance(userAddress, spenderAddress)
}