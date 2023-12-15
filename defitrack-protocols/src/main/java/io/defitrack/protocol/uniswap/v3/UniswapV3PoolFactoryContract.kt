package io.defitrack.uniswap.v3

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint24
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EventUtils.extract
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

class UniswapV3PoolFactoryContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    val poolCreatedEvent = Event(
        "PoolCreated", listOf(
            address(true),
            address(true),
            TypeUtils.uint24(true),
            TypeUtils.int24(),
            address(false)
        )
    )

    suspend fun getPools(fromBlock: String): List<String> {
        val logs = blockchainGateway.getEventsAsEthLog(
            GetEventLogsCommand(
                addresses = listOf(this.address),
                topic = "0x783cca1c0412dd0d695e784568c96da2e9c22ff989357a2e8b1d9b2b4e6b7118",
                fromBlock = BigInteger(fromBlock, 10),
            )
        )

        return logs.map {
            poolCreatedEvent.extract(it, false, 1) as String
        }
    }


    suspend fun getPool(
        token0: String,
        token1: String,
        fee: BigInteger
    ): String {
        return read(
            "getPool",
            listOf(
                token0.toAddress(),
                token1.toAddress(),
                fee.toUint24()
            ),
            listOf(address())
        )[0].value as String
    }
}

