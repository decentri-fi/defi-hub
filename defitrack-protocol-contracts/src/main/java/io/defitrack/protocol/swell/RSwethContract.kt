package io.defitrack.protocol.swell

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.position.Position
import io.defitrack.evm.position.PositionFetcher
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

class RSwethContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    //TODO: create provider

    //TODO: rate is currently not updated with refreshables
    val rate = constant<BigInteger>("getRate", uint256())
    val totalEthDeposited = constant<BigInteger>("totalETHDeposited", uint256())

    suspend fun positionFetcher() = PositionFetcher(
        this::balanceOfFunction, balanceOfExtraction(rate.await())
    )

    private fun balanceOfExtraction(rate: BigInteger): suspend (List<Type<*>>) -> Position =
        { result ->
            val bal = result[0].value as BigInteger
            if (bal > BigInteger.ZERO) {
                Position(bal.times(rate).asEth().toBigInteger(), bal)
            } else {
                Position.ZERO
            }
        }

}