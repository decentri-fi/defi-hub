package io.defitrack.rest

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class ERC1155Service(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun balanceOf(
        contract: String,
        address: String,
        tokenId: BigInteger,
        network: Network
    ): BigInteger {
        return try {
            blockchainGatewayProvider.getGateway(network).run {
                (readFunction(
                    address = contract,
                    function = "balanceOf",
                    inputs = listOf(
                        address.toAddress(),
                        tokenId.toUint256()
                    ),
                    outputs = listOf(TypeUtils.uint256())
                )[0].value as BigInteger)
            }
        } catch (ex: Exception) {
            logger.error("Error while getting balance of $address for token $tokenId on $network", ex)
            return BigInteger.ZERO
        }
    }

}