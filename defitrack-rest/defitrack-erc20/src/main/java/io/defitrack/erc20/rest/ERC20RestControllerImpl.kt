package io.defitrack.erc20.rest

import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.erc20.*
import io.defitrack.token.TokenType
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.web3j.crypto.WalletUtils
import java.math.BigInteger

@RestController
class ERC20RestControllerImpl(
    private val erC20ContractReader: ERC20ContractReader,
    private val erc20Service: ERC20Service,
    private val observationRegistry: ObservationRegistry
) : ERC20RestController {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{network}")
    override suspend fun getAllTokensForNetwork(
        @PathVariable("network") networkName: String,
    ): ResponseEntity<List<TokenInformationVO>> = coroutineScope {
        val network = Network.fromString(networkName) ?: return@coroutineScope ResponseEntity.badRequest().build()
        ResponseEntity.ok(
            erc20Service.getAllTokensForNetwork(network).map {
                it.toVO()
            }.filter {
                it.type == TokenType.SINGLE.name
            }
        )
    }

    @GetMapping("/{network}/wrapped")
    override fun getWrappedToken(@PathVariable("network") networkName: String): ResponseEntity<WrappedToken> {
        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        return ResponseEntity.ok(
            WrappedToken(
                ERC20Repository.NATIVE_WRAP_MAPPING[network]!!
            )
        )
    }

    @GetMapping("/{network}/{address}/token")
    override suspend fun getTokenInformation(
        @PathVariable("network") networkName: String,
        @PathVariable("address") address: String
    ): ResponseEntity<TokenInformationVO> = coroutineScope {
        val network = Network.fromString(networkName) ?: return@coroutineScope ResponseEntity.badRequest().build()
        val observation = Observation.start("erc20.get-token-information", observationRegistry)
            .lowCardinalityKeyValue("network", networkName)
        try {
            erc20Service.getTokenInformation(
                address, network
            ).map {
                ResponseEntity.ok(
                    it.toVO()
                )
            }.getOrElse {
                ResponseEntity.notFound().build()
            }
        } catch (ex: Exception) {
            logger.debug("Error while getting token information", ex)
            ResponseEntity.notFound().build()
        } finally {
            observation.stop()
        }
    }

    @GetMapping("/{network}/{address}/{userAddress}")
    override suspend fun getBalance(
        @PathVariable("network") networkName: String,
        @PathVariable("address") address: String,
        @PathVariable("userAddress") userAddress: String
    ): ResponseEntity<BigInteger> {

        val network = Network.fromString(networkName) ?: return ResponseEntity.badRequest().build()

        if (!WalletUtils.isValidAddress(address)) {
            return ResponseEntity.badRequest().build()
        }
        if (!WalletUtils.isValidAddress(userAddress)) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            ResponseEntity.ok(erC20ContractReader.getBalance(network, address, userAddress))
        } catch (ex: Exception) {
            ex.printStackTrace()
            ResponseEntity.ok(BigInteger.ZERO)
        }
    }

    @GetMapping("/{network}/allowance/{token}/{userAddress}/{spenderAddress}")
    override suspend fun getApproval(
        @PathVariable("network") networkName: String,
        @PathVariable("token") token: String,
        @PathVariable("userAddress") userAddress: String,
        @PathVariable("spenderAddress") spenderAddress: String,
    ): ResponseEntity<BigInteger> = coroutineScope {

        val network = Network.fromString(networkName) ?: return@coroutineScope ResponseEntity.badRequest().build()

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