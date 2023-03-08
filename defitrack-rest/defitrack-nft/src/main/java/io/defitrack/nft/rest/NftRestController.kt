package io.defitrack.nft.rest

import io.defitrack.common.network.Network
import io.defitrack.nft.service.ERC1155Service
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigInteger

@RestController
class NftRestController(
    private val erC1155Service: ERC1155Service
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{nft}/{address}/balance-of/{tokenId}")
    @Deprecated("Use ERC-specific endpoints now")
    fun getBalance(
        @PathVariable("nft") nft: String,
        @PathVariable("address") address: String,
        @PathVariable("tokenId") tokenId: BigInteger,
        @RequestParam("network") network: Network
    ): Map<String, Any> = runBlocking {
        mapOf(
            "balance" to erC1155Service.balanceOf(nft, address, tokenId, network)
        )
    }

    @GetMapping("/erc1155/{nft}/{address}/balance-of/{tokenId}")
    fun erc1155(
        @PathVariable("nft") nft: String,
        @PathVariable("address") address: String,
        @PathVariable("tokenId") tokenId: BigInteger,
        @RequestParam("network") network: Network
    ): Map<String, Any> = runBlocking {
        mapOf(
            "balance" to erC1155Service.balanceOf(nft, address, tokenId, network)
        )
    }


}