package io.defitrack.nft.service

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC1155Contract
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
        user: String,
        tokenId: BigInteger,
        network: Network
    ): BigInteger {
        return try {
            ERC1155Contract(
                blockchainGatewayProvider.getGateway(network), contract
            ).balanceOf(user, tokenId)
        } catch (ex: Exception) {
            logger.error("Error while getting balance of $user for token $tokenId on $network", ex)
            return BigInteger.ZERO
        }
    }
}