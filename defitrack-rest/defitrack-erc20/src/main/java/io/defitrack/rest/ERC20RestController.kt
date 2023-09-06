package io.defitrack.rest

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.erc20.ERC20Repository
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.erc20.TokenService
import io.defitrack.toVO
import kotlinx.coroutines.coroutineScope
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
    suspend fun getAllTokensForNetwork(
        @PathVariable("network") network: Network,
    ): ResponseEntity<List<TokenInformationVO>> = coroutineScope {
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
    suspend fun getTokenInformation(
        @PathVariable("network") network: Network,
        @PathVariable("address") address: String
    ): ResponseEntity<TokenInformationVO> = coroutineScope {
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
    suspend fun getBalance(
        @PathVariable("network") network: Network,
        @PathVariable("address") address: String,
        @PathVariable("userAddress") userAddress: String
    ): ResponseEntity<BigInteger> = coroutineScope {

        if (!WalletUtils.isValidAddress(address)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }

        return@coroutineScope try {
            ResponseEntity.ok(erC20ContractReader.getBalance(network, address, userAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }

    @GetMapping("/{network}/allowance/{token}/{userAddress}/{spenderAddress}")
    suspend fun getApproval(
        @PathVariable("network") network: Network,
        @PathVariable("token") token: String,
        @PathVariable("userAddress") userAddress: String,
        @PathVariable("spenderAddress") spenderAddress: String,
    ): ResponseEntity<BigInteger> = coroutineScope {

        if (!WalletUtils.isValidAddress(token)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(spenderAddress)) {
            return@coroutineScope ResponseEntity.badRequest().build()
        }

        return@coroutineScope try {
            ResponseEntity.ok(erC20ContractReader.getAllowance(network, token, userAddress, spenderAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }
}