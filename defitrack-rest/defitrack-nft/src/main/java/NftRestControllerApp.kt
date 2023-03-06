package io.defitrack

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger

@RestController
class NftRestControllerApp(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{nft}/{address}/balance-of/{tokenId}")
    fun getBalance(
        @PathVariable("nft") nft: String,
        @PathVariable("address") address: String,
        @PathVariable("tokenId") tokenId: BigInteger,
        @RequestParam("network") network: Network
    ): Map<String, Any> = runBlocking {
        mapOf(
            "balance" to getBalance(network, nft, address, tokenId)
        )
    }

    private suspend fun getBalance(
        network: Network,
        nft: String,
        address: String,
        tokenId: BigInteger
    ): Long {
        return try {
            blockchainGatewayProvider.getGateway(network).run {
                (readFunction(
                    address = nft,
                    function = "balanceOf",
                    inputs = listOf(
                        address.toAddress(),
                        tokenId.toUint256()
                    ),
                    outputs = listOf(uint256())
                )[0].value as BigInteger).toLong()
            }
        } catch (ex: Exception) {
            logger.error("Error while getting balance of $address for token $tokenId on $network", ex)
            return 0
        }
    }
}