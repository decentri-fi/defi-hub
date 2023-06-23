package io.defitrack.rest

import io.defitrack.common.network.Network
import io.defitrack.erc20.*
import io.defitrack.toVO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
class ERC20RestController(
    private val erC20ContractReader: ERC20ContractReader,
    private val tokenService: TokenService
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{network}")
    fun getAllTokensForNetwork(
        @PathVariable("network") network: Network,
    ): ResponseEntity<List<TokenInformationVO>> = runBlocking {
        ResponseEntity.ok(
            tokenService.getAllTokensForNetwork(network).map {
                it.toVO()
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
    ): ResponseEntity<TokenInformationVO> = runBlocking {
        try {
            withTimeout(12000L) {
                ResponseEntity.ok(
                    tokenService.getTokenInformation(
                        address, network
                    ).toVO()
                )
            }
        } catch (ex: Exception) {
            logger.error("Error while getting token information", ex)
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
            ResponseEntity.ok(erC20ContractReader.getBalance(network, address, userAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }

    @GetMapping("/{network}/allowance/{token}/{userAddress}/{spenderAddress}")
    fun getApproval(
        @PathVariable("network") network: Network,
        @PathVariable("token") token: String,
        @PathVariable("userAddress") userAddress: String,
        @PathVariable("spenderAddress") spenderAddress: String,
    ): ResponseEntity<BigInteger> = runBlocking {

        if (!WalletUtils.isValidAddress(token)) {
            return@runBlocking ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return@runBlocking ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(spenderAddress)) {
            return@runBlocking ResponseEntity.badRequest().build()
        }

        return@runBlocking try {
            ResponseEntity.ok(erC20ContractReader.getAllowance(network, token, userAddress, spenderAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }
}