package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.token.TokenInformation
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
class ERC20RestController(
    private val erC20Service: ERC20Service,
    private val tokenService: TokenService
) {

    @GetMapping("/{network}")
    fun getAllTokensForAddress(
        @PathVariable("network") network: Network,
    ): ResponseEntity<List<TokenInformation>> {
        return ResponseEntity.ok(
            runBlocking {
                tokenService.getAllTokensForNetwork(network)
            }
        )
    }

    @GetMapping("/{network}/wrapped")
    fun getWrappedToken(@PathVariable("network") network: Network): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "address" to ERC20Repository.NATIVE_WRAP_MAPPING[network]!!
            )
        )
    }

    @GetMapping("/{network}/{address}/token")
    fun getTokenInformation(
        @PathVariable("network") network: Network,
        @PathVariable("address") address: String
    ): ResponseEntity<TokenInformation> {
        return try {
            ResponseEntity.ok(
                runBlocking {
                    tokenService.getTokenInformation(
                        address, network
                    )
                }
            )
        } catch (ex: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{network}/{address}/{userAddress}")
    fun getBalance(
        @PathVariable("network") network: Network,
        @PathVariable("address") address: String,
        @PathVariable("userAddress") userAddress: String
    ): ResponseEntity<BigInteger> = runBlocking {

        if (!WalletUtils.isValidAddress(address)) {
            return@runBlocking ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return@runBlocking ResponseEntity.badRequest().build()
        }

        return@runBlocking try {
            ResponseEntity.ok(erC20Service.getBalance(network, address, userAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }
}